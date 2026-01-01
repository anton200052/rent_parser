package me.vasylkov.rentparser.component;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class ImmoScoutLinkConverter {
    private final Map<String, String> PARAM_NAME_MAP = Map.ofEntries(
            Map.entry("heatingtypes", "heatingtypes"),
            Map.entry("haspromotion", "haspromotion"),
            Map.entry("numberofrooms", "numberofrooms"),
            Map.entry("livingspace", "livingspace"),
            Map.entry("energyefficiencyclasses", "energyefficiencyclasses"),
            Map.entry("exclusioncriteria", "exclusioncriteria"),
            Map.entry("equipment", "equipment"),
            Map.entry("petsallowedtypes", "petsallowedtypes"),
            Map.entry("price", "price"),
            Map.entry("constructionyear", "constructionyear"),
            Map.entry("apartmenttypes", "apartmenttypes"),
            Map.entry("pricetype", "pricetype"),
            Map.entry("floor", "floor"),
            Map.entry("geocodes", "geocodes"),
            Map.entry("geocoordinates", "geocoordinates"),
            Map.entry("shape", "shape"),
            Map.entry("sorting", "sorting"),
            Map.entry("newbuilding", "newbuilding")
    );

    private final Map<String, String> EQUIPMENT_MAP = Map.ofEntries(
            Map.entry("parking", "parking"),
            Map.entry("cellar", "cellar"),
            Map.entry("builtinkitchen", "builtInKitchen"),
            Map.entry("lift", "lift"),
            Map.entry("garden", "garden"),
            Map.entry("guesttoilet", "guestToilet"),
            Map.entry("balcony", "balcony"),
            Map.entry("handicappedaccessible", "handicappedAccessible")
    );

    private final Map<String, String> REAL_ESTATE_TYPE = Map.ofEntries(
            Map.entry("haus-mieten", "houserent"),
            Map.entry("wohnung-mieten", "apartmentrent"),
            Map.entry("wohnung-kaufen", "apartmentbuy"),
            Map.entry("haus-kaufen", "housebuy")
    );

    // SEO-пути → дополнительные параметры + тип "wohnung-mieten"
    private final Map<String, Map<String, Object>> WEB_PATH_TO_APARTMENT_EQUIPMENT_MAP = Map.ofEntries(
            // Balkon/Terrasse
            Map.entry("wohnung-mit-balkon-mieten", mapOf("equipment", List.of("balcony"))),
            Map.entry("wohnung-mit-garten-mieten", mapOf("equipment", List.of("garden"))),
            // Wohnungstyp
            Map.entry("souterrainwohnung-mieten", mapOf("apartmenttypes", List.of("halfbasement"))),
            Map.entry("erdgeschosswohnung-mieten", mapOf("apartmenttypes", List.of("groundfloor"))),
            Map.entry("hochparterrewohnung-mieten", mapOf("apartmenttypes", List.of("raisedgroundfloor"))),
            Map.entry("etagenwohnung-mieten", mapOf("apartmenttypes", List.of("apartment"))),
            Map.entry("loft-mieten", mapOf("apartmenttypes", List.of("loft"))),
            Map.entry("maisonette-mieten", mapOf("apartmenttypes", List.of("maisonette"))),
            Map.entry("terrassenwohnung-mieten", mapOf("apartmenttypes", List.of("terracedflat"))),
            Map.entry("penthouse-mieten", mapOf("apartmenttypes", List.of("penthouse"))),
            Map.entry("dachgeschosswohnung-mieten", mapOf("apartmenttypes", List.of("roofstorey"))),
            // Ausstattung
            Map.entry("wohnung-mit-garage-mieten", mapOf("equipment", List.of("parking"))),
            Map.entry("wohnung-mit-einbaukueche-mieten", mapOf("equipment", List.of("builtinkitchen"))),
            Map.entry("wohnung-mit-keller-mieten", mapOf("equipment", List.of("cellar"))),
            // Merkmale
            Map.entry("neubauwohnung-mieten", mapOf("newbuilding", true)),
            Map.entry("barrierefreie-wohnung-mieten", mapOf("equipment", List.of("handicappedaccessible")))
    );

    private Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(key, value);
        return m;
    }

    public String convertWebToMobile(String webUrl) {
        Objects.requireNonNull(webUrl, "webUrl must not be null");

        final URI uri = parseUri(webUrl);
        final String[] segments = trimAndSplitPath(uri.getPath());

        if (segments.length < 1 || !"Suche".equals(segments[0])) {
            throw new IllegalArgumentException("Unexpected path format: " + uri.getPath() +
                    ". We're expecting to see \"/Suche\" in the path.");
        }

        // Проверка на shape
        if (Arrays.asList(segments).contains("shape")) {
            throw new IllegalArgumentException("Shape is currently not supported using Immoscout");
        }

        final String realTypeKey = segments[segments.length - 1];
        String realType = REAL_ESTATE_TYPE.get(realTypeKey);
        Map<String, Object> additionalFromSeo = null;

        if (realType == null) {
            // SEO-оптимизированные пути для квартир
            if (WEB_PATH_TO_APARTMENT_EQUIPMENT_MAP.containsKey(realTypeKey)) {
                additionalFromSeo = WEB_PATH_TO_APARTMENT_EQUIPMENT_MAP.get(realTypeKey);
                realType = REAL_ESTATE_TYPE.get("wohnung-mieten"); // apartmentrent
            } else {
                throw new IllegalArgumentException("Real estate type not found: " + realTypeKey);
            }
        }

        // Парсим query → Map<String, List<String>>
        Map<String, List<String>> rawParams = parseQueryParams(uri.getRawQuery());

        // Оставляем только поддерживаемые параметры и выкидываем enteredFrom
        Map<String, List<String>> webParams = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> e : rawParams.entrySet()) {
            String key = e.getKey();
            if ("enteredFrom".equalsIgnoreCase(key)) continue;
            if (PARAM_NAME_MAP.containsKey(key)) {
                webParams.put(key, e.getValue());
            }
        }

        boolean isRadius = Arrays.asList(segments).contains("radius");

        String geocodes = null;

        // Условие: должно быть как минимум 3 сегмента (Suche, geo-сегмент, тип)
        if (!isRadius && segments.length > 2) {
            // Берем все сегменты, начиная с индекса 1 (после "Suche")
            // и заканчивая предпоследним (не включая segments.length - 1).
            // Последний сегмент (segments.length - 1) - это realTypeKey.
            List<String> geoPathSegments = Arrays.asList(segments)
                    .subList(1, segments.length - 1);

            if (!geoPathSegments.isEmpty()) {
                geocodes = "/" + String.join("/", geoPathSegments);
            }
        }


        // Базовые мобильные параметры (LinkedHashMap для стабильного порядка)
        Map<String, Object> mobileParams = new LinkedHashMap<>();
        mobileParams.put("searchType", isRadius ? "radius" : "region");
        mobileParams.put("realestatetype", realType);
        if (!isRadius && geocodes != null) {
            mobileParams.put("geocodes", geocodes);
        }
        if (additionalFromSeo != null) {
            mergeAdditional(mobileParams, additionalFromSeo); // equipment/newbuilding/apartmenttypes
        }

        // Если у веб есть geocoordinates — переносим
        if (webParams.containsKey("geocoordinates")) {
            String geo = firstOrNull(webParams.get("geocoordinates"));
            if (!isNullOrEmpty(geo)) {
                mobileParams.put("geocoordinates", geo);
            }
        }

        // Перенос остальных параметров
        for (Map.Entry<String, List<String>> e : webParams.entrySet()) {
            String webKey = e.getKey();
            List<String> values = e.getValue();

            if ("geocoordinates".equals(webKey)) continue; // уже перенесли
            String mobileKey = PARAM_NAME_MAP.get(webKey);
            if (mobileKey == null) continue;

            if ("equipment".equals(webKey)) {
                // нормализуем список (могут быть через запятую)
                List<String> items = splitCommaValues(values);
                // маппим на мобильные значения
                List<String> mapped = new ArrayList<>();
                for (String item : items) {
                    String v = item == null ? null : item.trim().toLowerCase(Locale.ROOT);
                    if (v == null || v.isEmpty()) continue;
                    String mappedVal = EQUIPMENT_MAP.get(v);
                    if (mappedVal != null) mapped.add(mappedVal);
                }
                if (!mapped.isEmpty()) {
                    // если раньше что-то добавили (например, из SEO) — склеиваем
                    Object existing = mobileParams.get(mobileKey);
                    List<String> acc = new ArrayList<>();
                    if (existing instanceof List) {
                        acc.addAll((List<String>) existing);
                    }
                    acc.addAll(mapped);
                    mobileParams.put(mobileKey, acc);
                }
            } else {
                // обычные значения: берём первый (как делает исходник, там строки)
                String val = firstOrNull(values);
                if (!isNullOrEmpty(val)) {
                    mobileParams.put(mobileKey, val);
                }
            }
        }

        mobileParams.put("sorting", "-firstactivation");

        // Сборка итогового URL
        String query = buildQueryString(mobileParams);
        return "https://api.mobile.immobilienscout24.de/search/list?" + query;
    }

    public String convertImmoscoutListingToMobileListing(String url) {
        if (isNullOrEmpty(url)) return null;
        return url.replaceFirst("^https://www\\.immobilienscout24\\.de/expose/", "https://api.mobile.immobilienscout24.de/expose/");
    }

    /* -------------------- Helpers -------------------- */

    private URI parseUri(String s) {
        try {
            return new URI(s);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + s, e);
        }
    }

    private String[] trimAndSplitPath(String path) {
        if (path == null || path.isEmpty()) return new String[]{""};
        String p = path;
        if (p.startsWith("/")) p = p.substring(1);
        if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
        return p.isEmpty() ? new String[]{""} : p.split("/");
    }

    private Map<String, List<String>> parseQueryParams(String rawQuery) {
        Map<String, List<String>> out = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) return out;

        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            if (pair.isEmpty()) continue;
            String[] kv = pair.split("=", 2);
            String k = urlDecode(kv[0]);
            String v = kv.length > 1 ? urlDecode(kv[1]) : "";
            out.computeIfAbsent(k, __ -> new ArrayList<>()).add(v);
        }
        return out;
    }

    private String urlDecode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    private String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private String firstOrNull(List<String> list) {
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    private List<String> splitCommaValues(List<String> values) {
        List<String> out = new ArrayList<>();
        if (values == null) return out;
        for (String v : values) {
            if (v == null) continue;
            String[] parts = v.split(",");
            for (String p : parts) {
                String t = p.trim();
                if (!t.isEmpty()) out.add(t);
            }
        }
        return out;
    }

    private void mergeAdditional(Map<String, Object> target, Map<String, Object> add) {
        for (Map.Entry<String, Object> e : add.entrySet()) {
            String k = e.getKey();
            Object v = e.getValue();
            if ("equipment".equals(k) || "apartmenttypes".equals(k)) {
                // списки склеиваем
                List<String> toAdd = (v instanceof List) ? (List<String>) v : List.of(String.valueOf(v));
                Object existing = target.get(k);
                List<String> acc = new ArrayList<>();
                if (existing instanceof List) acc.addAll((List<String>) existing);
                acc.addAll(toAdd);
                target.put(k, acc);
            } else {
                target.put(k, v);
            }
        }
    }

    /**
     * Сборка query:
     * - List<String> → join(",")
     * - boolean → "true"/"false"
     * - другие → строка как есть
     * - пропускаем пустые строки
     */
    private String buildQueryString(Map<String, Object> params) {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, Object> e : params.entrySet()) {
            String key = e.getKey();
            Object val = e.getValue();
            if (val == null) continue;

            String strVal;
            if (val instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> list = (List<String>) val;
                if (list.isEmpty()) continue;
                strVal = String.join(",", list);
            } else if (val instanceof Boolean) {
                strVal = Boolean.TRUE.equals(val) ? "true" : "false";
            } else {
                strVal = String.valueOf(val);
            }

            if (isNullOrEmpty(strVal)) continue;
            parts.add(urlEncode(key) + "=" + urlEncode(strVal));
        }
        return String.join("&", parts);
    }
}

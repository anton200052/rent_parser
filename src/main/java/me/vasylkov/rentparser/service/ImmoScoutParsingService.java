package me.vasylkov.rentparser.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.vasylkov.rentparser.component.ImmoScoutLinkConverter;
import me.vasylkov.rentparser.dto.ImmoScoutSearchResponse;
import me.vasylkov.rentparser.entity.ImmoScoutListing;
import me.vasylkov.rentparser.entity.TaskInfo;
import me.vasylkov.rentparser.entity.Listing;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Setter
@Service
@RequiredArgsConstructor
public class ImmoScoutParsingService implements ParsingService {
    private final RestTemplate restTemplate;
    private final ImmoScoutLinkConverter immoScoutLinkConverter;

    @Override
    public TaskInfo.ProviderType supportedType() {
        return TaskInfo.ProviderType.IMMO_SCOUT;
    }

    @Override
    public List<Listing> parseRentWebSite(String providerUrl) {
        ImmoScoutSearchResponse immoScoutSearchResponse = getImmoScoutSearchResponse(providerUrl);
        if (immoScoutSearchResponse != null)
            return convertResponseToListings(immoScoutSearchResponse);

        return List.of();
    }

    private ImmoScoutSearchResponse getImmoScoutSearchResponse(String providerUrl) {
        URI uri = URI.create(immoScoutLinkConverter.convertWebToMobile(providerUrl));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Connection", "keep-alive");
        headers.set("User-Agent", "ImmoScout_27.3_26.0._-_");
        headers.set("Accept", "application/json");
        return restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<Void>(headers), ImmoScoutSearchResponse.class).getBody();
    }

    private List<Listing> convertResponseToListings(ImmoScoutSearchResponse immoScoutSearchResponse) {
        List<Listing> listings = new ArrayList<>();

        for (ImmoScoutSearchResponse.ResultEntry resultEntry : immoScoutSearchResponse.getResultListItems()) {
            ImmoScoutSearchResponse.Item item = resultEntry.getItem();

            Listing listing = convertItemToListing(item);

            listings.add(listing);
        }

        return listings;
    }

    private Listing convertItemToListing(ImmoScoutSearchResponse.Item item) {
        List<ImmoScoutSearchResponse.Attribute> attributes = item.getAttributes();

        ImmoScoutListing listing = new ImmoScoutListing();
        listing.setId(item.getId());
        listing.setPrice(attributes.get(0).getValue());
        listing.setAreaSqMeters(attributes.get(1).getValue());
        listing.setRoomsValue(attributes.get(2).getValue());
        listing.setDescription(item.getTitle());
        listing.setLocation(item.getAddress().getLine());
        listing.setPublished(item.getPublished());
        listing.setLink(generateListingLink(item.getId()));
        listing.setPlusRequired(item.isPrivateListing());
        return listing;
    }

    private String generateListingLink(String id) {
        return "https://www.immobilienscout24.de/expose/" + id;
    }
}

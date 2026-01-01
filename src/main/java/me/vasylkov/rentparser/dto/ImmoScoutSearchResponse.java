package me.vasylkov.rentparser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImmoScoutSearchResponse {

    private int totalResults;
    private int pageSize;
    private int pageNumber;
    private int numberOfPages;
    private int numberOfListings;

    private List<ResultEntry> resultListItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResultEntry {
        private String type;  // "EXPOSE_RESULT"
        private Item item;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String reportUrl;
        private String id;
        private String title;
        private String energyEfficiencyClass;
        private List<Picture> pictures;
        private TitlePicture titlePicture;
        private Address address;

        // В исходном JSON поля начинаются с "is*"
        @JsonProperty("isProject")
        private boolean project;

        @JsonProperty("isPrivate")
        private boolean privateListing; // нельзя называть "private"

        private String listingType;
        private String published;

        @JsonProperty("isNewObject")
        private boolean newObject;

        private boolean liveVideoTourAvailable;
        private boolean listOnlyOnIs24;

        private List<Attribute> attributes;
        private String realEstateType;
        private Realtor realtor;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Picture {
        private String urlScaleAndCrop;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TitlePicture {
        private String preview;
        private String full;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private String line;
        private double lat;
        private double lon;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attribute {
        private String label;
        private String value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Realtor {
        private String logoUrlScale;
        private String showcasePlacementColor;
    }
}

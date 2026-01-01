package me.vasylkov.rentparser.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ImmoScoutListing extends Listing {
    private Boolean plusRequired;

    public Listing toListingEntity() {
        return new Listing(
                this.getId(),
                this.getPrice(),
                this.getAreaSqMeters(),
                this.getRoomsValue(),
                this.getDescription(),
                this.getLocation(),
                this.getPublished(),
                this.getLink()
        );
    }
}

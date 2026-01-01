package me.vasylkov.rentparser.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "handled_listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Listing {
    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(name = "price", nullable = false)
    private String price;

    @Column(name = "area_sq_meters", nullable = false)
    private String areaSqMeters;

    @Column(name = "rooms_value", nullable = false)
    private String roomsValue;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "published", nullable = false)
    private String published;

    @Column(name = "link", nullable = false)
    private String link;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !(o instanceof Listing)) {
            return false;
        }

        Listing listing = (Listing) o;

        if (this.id == null) {
            return false;
        }

        return this.id.equals(listing.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

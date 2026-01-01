package me.vasylkov.rentparser.component;

import me.vasylkov.rentparser.entity.Listing;
import me.vasylkov.rentparser.model.TaskInfoSnapshot;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ListingsFilter {
    public List<Listing> filterListingsToRemoveHandled(List<Listing> listings, TaskInfoSnapshot taskInfo) {
        Set<String> handledIdsForThisTask = taskInfo.handledListingIds();

        return listings.stream()
                .filter(listing -> !handledIdsForThisTask.contains(listing.getId()))
                .toList();
    }
}

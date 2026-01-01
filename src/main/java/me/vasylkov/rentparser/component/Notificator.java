package me.vasylkov.rentparser.component;

import me.vasylkov.rentparser.entity.Listing;
import me.vasylkov.rentparser.model.TaskInfoSnapshot;

import java.util.List;

public interface Notificator {
    void sendListings(List<Listing> listings, TaskInfoSnapshot taskInfo);

    void sendMessage(String message, TaskInfoSnapshot taskInfo);
}

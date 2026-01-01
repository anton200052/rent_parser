package me.vasylkov.rentparser.service;

import me.vasylkov.rentparser.entity.Listing;

import java.util.List;
import java.util.Optional;

public interface HandledListingsService {
    Optional<Listing> findOptionalById(String id);
    Listing save(Listing listing);
    List<String> findExistingIds(List<String> ids);
    List<Listing> saveAll(List<Listing> listings);
}

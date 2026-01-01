package me.vasylkov.rentparser.service;

import lombok.RequiredArgsConstructor;
import me.vasylkov.rentparser.entity.Listing;
import me.vasylkov.rentparser.repository.HandledListingsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HandledListingsServiceImp implements HandledListingsService {
    private final HandledListingsRepository repo;

    @Override
    public Optional<Listing> findOptionalById(String id) {
        return repo.findById(id);
    }

    @Override
    public Listing save(Listing listing) {
        return repo.save(listing);
    }

    @Override
    public List<String> findExistingIds(List<String> ids) {
        return repo.findExistingIds(ids);
    }

    @Override
    public List<Listing> saveAll(List<Listing> listings) {
        return repo.saveAll(listings);
    }
}

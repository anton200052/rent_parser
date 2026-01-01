package me.vasylkov.rentparser.service;

import me.vasylkov.rentparser.entity.TaskInfo;
import me.vasylkov.rentparser.entity.Listing;

import java.util.List;

public interface ParsingService {
    TaskInfo.ProviderType supportedType();
    List<Listing> parseRentWebSite(String providerUrl);
}

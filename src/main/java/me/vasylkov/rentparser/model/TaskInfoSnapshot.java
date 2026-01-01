package me.vasylkov.rentparser.model;

import me.vasylkov.rentparser.entity.Listing;
import me.vasylkov.rentparser.entity.TaskInfo;
import me.vasylkov.rentparser.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record TaskInfoSnapshot(
        Long id,
        User user,
        TaskInfo.ProviderType providerType,
        String providerUrl,
        boolean active,
        String telegramToken,
        List<String> chatIds,
        long iterations,
        Set<String> handledListingIds
) {
    public static TaskInfoSnapshot from(TaskInfo taskInfo) {
        Set<String> ids = taskInfo.getHandledListings().stream()
                .map(Listing::getId)
                .collect(Collectors.toSet());

        return new TaskInfoSnapshot(
                taskInfo.getId(),
                taskInfo.getUser(),
                taskInfo.getProvider().getType(),
                taskInfo.getProvider().getProviderUrl(),
                taskInfo.isActive(),
                taskInfo.getTelegramNotification().getToken(),
                new ArrayList<>(taskInfo.getTelegramNotification().getChatIds()),
                taskInfo.getIterations(),
                ids
        );
    }
}

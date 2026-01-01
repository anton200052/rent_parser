package me.vasylkov.rentparser.service;

import lombok.extern.slf4j.Slf4j;
import me.vasylkov.rentparser.component.ListingsFilter;
import me.vasylkov.rentparser.component.Notificator;
import me.vasylkov.rentparser.entity.*;
import me.vasylkov.rentparser.model.TaskInfoSnapshot;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskRunnerServiceImp implements TaskRunnerService {
    @Qualifier("taskInfoServiceImp")
    private final TaskInfoService taskInfoService;
    @Qualifier("tasksExecutor")
    private final Executor tasksExecutor;
    @Qualifier("telegramMessagesSender")
    private final Notificator notificator;
    @Qualifier("userServiceImp")
    private final UserService userService;

    private final ListingsFilter listingsFilter;

    private final Map<TaskInfo.ProviderType, ParsingService> parsers;

    public TaskRunnerServiceImp(TaskInfoService taskInfoService, List<ParsingService> parsingServices, Executor tasksExecutor, Notificator notificator, UserService userService, ListingsFilter listingsFilter) {
        this.taskInfoService = taskInfoService;
        this.tasksExecutor = tasksExecutor;

        this.parsers = parsingServices.stream()
                .collect(Collectors.toMap(ParsingService::supportedType, Function.identity()));
        this.notificator = notificator;
        this.userService = userService;
        this.listingsFilter = listingsFilter;
    }

    @Override
    @Transactional
    public void checkWorkTimeAndRunAllExistingTasks() {
        LocalTime now = LocalTime.now();
        List<TaskInfo> activeTasks = taskInfoService.findAllActive();

        Map<User, Set<TaskInfo>> usersTasks = getTasksByEnabledUsers(activeTasks);

        for (Map.Entry<User, Set<TaskInfo>> entry : usersTasks.entrySet()) {
            User user = entry.getKey();
            Settings settings = user.getSettings();
            Set<TaskInfo> tasks = entry.getValue();

            LocalTime workFrom = LocalTime.parse(settings.getWorkFrom());
            LocalTime workUntil = LocalTime.parse(settings.getWorkUntil());

            if (now.isBefore(workFrom) || now.isAfter(workUntil)) {
                continue;
            }

            String lastScheduledRunStr = user.getLastScheduledRun();
            LocalDateTime lastRun = lastScheduledRunStr != null ? LocalDateTime.parse(lastScheduledRunStr) : null;
            int intervalMinutes = settings.getIntervalMinutes();

            if (lastRun != null && LocalDateTime.now().isBefore(lastRun.plusMinutes(intervalMinutes))) {
                continue;
            }

            if (!tasks.isEmpty()) {
                userService.updateLastScheduledRun(user.getId());
                runTasks(tasks);
            }
        }
    }

    @Override
    @Transactional
    public void runUserTasksNow(Long userId) {
        Optional<User> user = userService.findById(userId);
        user.ifPresent(value -> runTasks(value.getTasks()));
    }

    private void runTasks(Set<TaskInfo> tasks) {
        tasks.forEach(taskInfo -> {
            TaskInfoSnapshot taskInfoSnapshot = TaskInfoSnapshot.from(taskInfo);
            parseAndNotifyAsync(taskInfoSnapshot);
        });
    }

    private void parseAndNotifyAsync(TaskInfoSnapshot taskInfo) {
        TaskInfo.ProviderType type = taskInfo.providerType();
        ParsingService parsingService = parsers.get(type);
        if (parsingService == null || !taskInfo.active()) {
            return;
        }

        CompletableFuture
                .supplyAsync(() -> parsingService.parseRentWebSite(taskInfo.providerUrl()), tasksExecutor)
                .thenApply(listings -> listingsFilter.filterListingsToRemoveHandled(listings, taskInfo))
                .thenAccept(filteredListings -> {
                    notificator.sendListings(filteredListings, taskInfo);
                    List<Listing> listingsToSave = filteredListings.stream()
                            .map(listing -> {
                                if (listing instanceof ImmoScoutListing isl) {
                                    return isl.toListingEntity();
                                }
                                return listing;
                            })
                            .toList();

                    if (!listingsToSave.isEmpty()) {
                        taskInfoService.addHandledListingsToTask(taskInfo.id(), taskInfo.user(), listingsToSave);
                    }
                })
                .thenRun(() -> taskInfoService.incrementIterationsByIdAndUser(taskInfo.id(), taskInfo.user()))
                .exceptionally(ex -> {
                    log.error("Error while parsing {} site.", parsingService.supportedType(), ex);
                    notificator.sendMessage(
                            String.format("Error while parsing %s site.", parsingService.supportedType()), taskInfo);
                    return null;
                });
    }

    private Map<User, Set<TaskInfo>> getTasksByEnabledUsers(List<TaskInfo> activeTasks) {
        return activeTasks.stream()
                .filter(t -> t.getUser() != null && t.getUser().isEnabled())
                .collect(Collectors.groupingBy(TaskInfo::getUser, Collectors.toSet()));
    }
}
package me.vasylkov.rentparser.service;

import me.vasylkov.rentparser.entity.Listing;
import me.vasylkov.rentparser.entity.TaskInfo;
import me.vasylkov.rentparser.entity.User;

import java.util.List;
import java.util.Optional;

public interface TaskInfoService {
    List<TaskInfo> findByUser(User user);

    List<TaskInfo> findAllActive();

    Optional<TaskInfo> findByIdAndUser(Long id, User user);

    TaskInfo createFromForm(TaskInfo taskInfo, User user);

    TaskInfo updateFromForm(TaskInfo taskInfo, User user);

    void deleteByIdAndUser(Long id, User user);

    void incrementIterationsByIdAndUser(Long id, User user);

    void toggleActiveByIdAndUser(Long id, User user);

    void addHandledListingsToTask(Long taskId, User user, List<Listing> listings);
}
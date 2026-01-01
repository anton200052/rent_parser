package me.vasylkov.rentparser.service;

public interface TaskRunnerService {
    void checkWorkTimeAndRunAllExistingTasks();
    void runUserTasksNow(Long userId);
}

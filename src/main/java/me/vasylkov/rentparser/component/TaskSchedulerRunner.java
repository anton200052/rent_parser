package me.vasylkov.rentparser.component;

import jakarta.annotation.PostConstruct;
import me.vasylkov.rentparser.service.TaskRunnerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.time.Duration;


import org.springframework.scheduling.Trigger;

import jakarta.annotation.PreDestroy;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class TaskSchedulerRunner {

    private final ThreadPoolTaskScheduler scheduler;
    @Qualifier("taskRunnerServiceImp")
    private final TaskRunnerService taskRunnerService;

    private volatile ScheduledFuture<?> scheduledFuture;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean runningNow = new AtomicBoolean(false);
    private final Object monitor = new Object();

    public TaskSchedulerRunner(ThreadPoolTaskScheduler scheduler,
                               TaskRunnerService taskRunnerService) {
        this.scheduler = scheduler;
        this.taskRunnerService = taskRunnerService;
    }

    private void runTask() {
        if (!runningNow.compareAndSet(false, true)) {
            return;
        }
        try {
            taskRunnerService.checkWorkTimeAndRunAllExistingTasks();
        } finally {
            runningNow.set(false);
        }
    }

    private Trigger buildTrigger() {
        return new PeriodicTrigger(Duration.ofMinutes(1));
    }

    private void schedule() {
        synchronized (monitor) {
            scheduledFuture = scheduler.schedule(this::runTask, buildTrigger());
        }
    }

    private void cancelInternal() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
        }
    }

    @PostConstruct
    public void start() {
        if (!started.compareAndSet(false, true)) return;
        schedule();
    }

    @PreDestroy
    public void shutdown() {
        cancelInternal();
    }
}


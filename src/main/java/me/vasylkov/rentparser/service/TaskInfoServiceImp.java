package me.vasylkov.rentparser.service;

import me.vasylkov.rentparser.entity.Listing;
import me.vasylkov.rentparser.entity.TaskInfo;
import me.vasylkov.rentparser.entity.User;
import me.vasylkov.rentparser.repository.TaskInfoRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TaskInfoServiceImp implements TaskInfoService {
    private final TaskInfoRepository repo;

    @Qualifier("handledListingsServiceImp")
    private final HandledListingsService handledListingsService;

    public TaskInfoServiceImp(TaskInfoRepository repo, HandledListingsService handledListingsService) {
        this.repo = repo;
        this.handledListingsService = handledListingsService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskInfo> findByUser(User user) {
        return repo.findByUser(user);
    }

    @Override
    public List<TaskInfo> findAllActive() {
        return repo.findAllByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TaskInfo> findByIdAndUser(Long id, User user) {
        return repo.findByIdAndUser(id, user);
    }

    @Override
    @Transactional
    public TaskInfo createFromForm(TaskInfo taskInfo, User user) {
        taskInfo.setUser(user);
        taskInfo.setId(null);
        return repo.save(taskInfo);
    }

    @Override
    @Transactional
    public TaskInfo updateFromForm(TaskInfo taskInfo, User user) {
        Optional<TaskInfo> taskInfoOptional = repo.findByIdAndUser(taskInfo.getId(), user);
        if (taskInfoOptional.isPresent()) {
            TaskInfo existedTaskInfo = taskInfoOptional.get();

            existedTaskInfo.setProvider(taskInfo.getProvider());
            existedTaskInfo.setTelegramNotification(taskInfo.getTelegramNotification());

            return repo.save(existedTaskInfo);
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteByIdAndUser(Long id, User user) {
        Optional<TaskInfo> task = repo.findByIdAndUser(id, user);
        task.ifPresent(repo::delete);
    }

    @Override
    @Transactional
    public void incrementIterationsByIdAndUser(Long id, User user) {
        repo.incrementIterations(id, user);
    }

    @Override
    @Transactional
    public void toggleActiveByIdAndUser(Long id, User user) {
        repo.toggleActive(id, user);
    }

    @Override
    @Transactional
    public void addHandledListingsToTask(Long taskId, User user, List<Listing> listings) {
        TaskInfo task = repo.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        Set<Listing> handledListingsForTask = task.getHandledListings();

        for (Listing listingData : listings) {
            Listing managedListing = handledListingsService.findOptionalById(listingData.getId())
                    .orElseGet(() -> handledListingsService.save(listingData));

            handledListingsForTask.add(managedListing);
        }
    }
}
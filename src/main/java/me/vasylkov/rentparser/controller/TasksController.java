package me.vasylkov.rentparser.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.vasylkov.rentparser.component.HtmxChecker;
import me.vasylkov.rentparser.entity.Settings;
import me.vasylkov.rentparser.entity.TaskInfo;
import me.vasylkov.rentparser.entity.User;
import me.vasylkov.rentparser.service.SettingsService;
import me.vasylkov.rentparser.service.TaskInfoService;
import me.vasylkov.rentparser.service.TaskRunnerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/tasks")
public class TasksController {
    @Qualifier("taskInfoServiceImp")
    private final TaskInfoService taskInfoService;
    @Qualifier("settingsService")
    private final SettingsService settingsService;
    @Qualifier("taskRunnerServiceImp")
    private final TaskRunnerService taskRunnerService;
    private final HtmxChecker htmxChecker;

    public TasksController(TaskInfoService taskInfoService, HtmxChecker htmxChecker, SettingsService settingsService, TaskRunnerService taskRunnerService) {
        this.taskInfoService = taskInfoService;
        this.htmxChecker = htmxChecker;
        this.settingsService = settingsService;
        this.taskRunnerService = taskRunnerService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal User authenticatedUser, Model model, HttpServletRequest req) {
        addLayoutAttributes(model, authenticatedUser);
        return htmxChecker.isHtmx(req) ? "tasks/list :: body" : "layout";
    }

    @GetMapping("/new")
    public String newTaskForm(@AuthenticationPrincipal User authenticatedUser, Model model, HttpServletRequest req) {
        if (htmxChecker.isHtmx(req)) {
            model.addAttribute("taskInfo", new TaskInfo());
            return "tasks/task-form :: body";
        }
        addLayoutAttributes(model, authenticatedUser);
        return "layout";
    }

    @GetMapping("/{id}/edit")
    public String editTaskForm(@AuthenticationPrincipal User authenticatedUser, @PathVariable Long id, Model model, HttpServletRequest req, HttpServletResponse res) {
        if (htmxChecker.isHtmx(req)) {
            Optional<TaskInfo> optionalTaskInfo = taskInfoService.findByIdAndUser(id, authenticatedUser);
            if (optionalTaskInfo.isPresent()) {
                TaskInfo taskInfo = optionalTaskInfo.get();
                model.addAttribute("taskInfo", taskInfo);
                return "tasks/task-form :: body";
            } else {
                res.setHeader("HX-Redirect", "/tasks");
                return "layout";
            }
        }
        addLayoutAttributes(model, authenticatedUser);
        return "layout";
    }

    @PostMapping
    public String upsert(@AuthenticationPrincipal User authenticatedUser, @ModelAttribute TaskInfo taskInfo, Model model, HttpServletRequest req) {
        if (taskInfo.getId() == null) {
            taskInfoService.createFromForm(taskInfo, authenticatedUser);
        } else {
            taskInfoService.updateFromForm(taskInfo, authenticatedUser);
        }
        addLayoutAttributes(model, authenticatedUser);
        return "tasks/list :: body";
    }

    @PostMapping("/{id}/toggle")
    public String toggleTask(@AuthenticationPrincipal User authenticatedUser, @PathVariable Long id, Model model, HttpServletRequest req) {
        taskInfoService.toggleActiveByIdAndUser(id, authenticatedUser);
        addLayoutAttributes(model, authenticatedUser);
        return "tasks/list :: body";
    }

    @PostMapping("/searchNow")
    public String searchNow(@AuthenticationPrincipal User authenticatedUser, Model model, HttpServletRequest req) {
        taskRunnerService.runUserTasksNow(authenticatedUser.getId());
        addLayoutAttributes(model, authenticatedUser);
        return "tasks/list :: body";
    }

    @PostMapping("/{id}/delete")
    public String deleteTask(@AuthenticationPrincipal User authenticatedUser, @PathVariable Long id, Model model, HttpServletRequest req) {
        taskInfoService.deleteByIdAndUser(id, authenticatedUser);
        addLayoutAttributes(model, authenticatedUser);
        return "tasks/list :: body";
    }

    private void addLayoutAttributes(Model model, User user) {
        model.addAttribute("tasks", taskInfoService.findByUser(user));

        Optional<Settings> optionalSettings = settingsService.getSettingsByUser(user);
        model.addAttribute("interval", optionalSettings.map(Settings::getIntervalMinutes).orElse(0));
    }
}
package me.vasylkov.rentparser.controller;

import me.vasylkov.rentparser.entity.Settings;
import me.vasylkov.rentparser.entity.User;
import me.vasylkov.rentparser.service.SettingsService;
import me.vasylkov.rentparser.service.TaskInfoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class RootController {
    @Qualifier("taskInfoServiceImp")
    private final TaskInfoService taskInfoService;
    @Qualifier("settingsService")
    private final SettingsService settingsService;

    public RootController(TaskInfoService taskInfoService, SettingsService settingsService) {
        this.taskInfoService = taskInfoService;
        this.settingsService = settingsService;
    }

    @GetMapping("/")
    public String layout(@AuthenticationPrincipal User authenticatedUser, Model model) {
        model.addAttribute("tasks", taskInfoService.findByUser(authenticatedUser));

        Optional<Settings> optionalSettings = settingsService.getSettingsByUser(authenticatedUser);
        model.addAttribute("interval", optionalSettings.map(Settings::getIntervalMinutes).orElse(0));
        return "layout";
    }
}

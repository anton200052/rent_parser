package me.vasylkov.rentparser.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.vasylkov.rentparser.component.HtmxChecker;
import me.vasylkov.rentparser.entity.Settings;
import me.vasylkov.rentparser.entity.User;
import me.vasylkov.rentparser.service.SettingsService;
import me.vasylkov.rentparser.service.TaskInfoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/settings")
public class SettingsController {
    @Qualifier("taskInfoServiceImp")
    private final TaskInfoService taskInfoService;
    @Qualifier("settingsService")
    private final SettingsService settingsService;
    private final HtmxChecker htmxChecker;

    public SettingsController(TaskInfoService taskInfoService, SettingsService settingsService, HtmxChecker htmxChecker) {
        this.taskInfoService = taskInfoService;
        this.settingsService = settingsService;
        this.htmxChecker = htmxChecker;
    }

    @GetMapping
    public String page(@AuthenticationPrincipal User authenticatedUser, Model model, HttpServletRequest req, HttpServletResponse res) {
        Optional<Settings> optionalSettings = settingsService.getSettingsByUser(authenticatedUser);
        if (htmxChecker.isHtmx(req)) {
            if (optionalSettings.isPresent()) {
                Settings settings = optionalSettings.get();
                model.addAttribute("settings", settings);
                return "settings/index :: body";
            } else {
                res.setHeader("HX-Redirect", "/users");
                return "layout";
            }
        }

        model.addAttribute("tasks", taskInfoService.findByUser(authenticatedUser));
        model.addAttribute("interval", optionalSettings.map(Settings::getIntervalMinutes).orElse(0));
        return "layout";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<Void> update(@AuthenticationPrincipal User authenticatedUser, @ModelAttribute Settings settings) {
        settingsService.updateSettingsFromForm(settings, authenticatedUser);
        return ResponseEntity.ok()
                .header("HX-Trigger", "{\"showToast\": {\"message\": \"" + "Settings saved!" + "\"}}")
                .build();
    }
}

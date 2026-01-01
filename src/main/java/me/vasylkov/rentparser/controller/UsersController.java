package me.vasylkov.rentparser.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.vasylkov.rentparser.component.HtmxChecker;
import me.vasylkov.rentparser.entity.Settings;
import me.vasylkov.rentparser.entity.User;
import me.vasylkov.rentparser.exception.UserNotFoundException;
import me.vasylkov.rentparser.service.RoleService;
import me.vasylkov.rentparser.service.SettingsService;
import me.vasylkov.rentparser.service.TaskInfoService;
import me.vasylkov.rentparser.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UsersController {
    @Qualifier("userServiceImp")
    private final UserService userService;
    @Qualifier("taskInfoServiceImp")
    private final TaskInfoService taskInfoService;
    @Qualifier("settingsServiceImp")
    private final SettingsService settingsService;
    @Qualifier("roleServiceImp")
    private final RoleService roleService;
    private final HtmxChecker htmxChecker;

    public UsersController(UserService userService, TaskInfoService taskInfoService, SettingsService settingsService, RoleService roleService, HtmxChecker htmxChecker) {
        this.userService = userService;
        this.taskInfoService = taskInfoService;
        this.settingsService = settingsService;
        this.roleService = roleService;
        this.htmxChecker = htmxChecker;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal User authenticatedUser, Model model, HttpServletRequest req) {
        if (htmxChecker.isHtmx(req)) {
            addUsersListAttributes(model);
            return "users/users :: body";
        }

        addLayoutAttributes(model, authenticatedUser);
        return "layout";
    }

    @GetMapping("/new")
    public String newUsersForm(@AuthenticationPrincipal User authenticatedUser, Model model, HttpServletRequest req) {
        if (htmxChecker.isHtmx(req)) {
            model.addAttribute("roles", roleService.findAll());
            model.addAttribute("user", new User());
            return "users/users-form :: body";
        }
        addLayoutAttributes(model, authenticatedUser);
        return "layout";
    }

    @GetMapping("/{id}/edit")
    public String editUsersForm(@AuthenticationPrincipal User authenticatedUser, @PathVariable Long id, Model model, HttpServletRequest req, HttpServletResponse res) {
        if (htmxChecker.isHtmx(req)) {
            Optional<User> optionalUser = userService.findById(id);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setPassword("");
                model.addAttribute("roles", roleService.findAll());
                model.addAttribute("user", user);
                return "users/users-form :: body";
            } else {
                res.setHeader("HX-Redirect", "/users");
                return "layout";
            }
        }
        addLayoutAttributes(model, authenticatedUser);
        return "layout";
    }

    @PostMapping
    public String upsert(@ModelAttribute User user, Model model, HttpServletResponse res) {
        if (user.getId() == null) {
            userService.createFromForm(user);
        } else {
            userService.editFromForm(user);
        }
        addUsersListAttributes(model);
        return "users/users :: body";
    }

    @PostMapping("/{id}/toggle")
    public String toggleUser(@PathVariable Long id, Model model) {
        userService.toggleEnabled(id);
        addUsersListAttributes(model);
        return "users/users :: body";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, Model model) {
        try {
            userService.deleteById(id);
        } catch (UserNotFoundException e) {
            //ignore
        }
        addUsersListAttributes(model);
        return "users/users :: body";
    }

    private void addLayoutAttributes(Model model, User authenticatedUser) {
        model.addAttribute("tasks", taskInfoService.findByUser(authenticatedUser));

        Optional<Settings> optionalSettings = settingsService.getSettingsByUser(authenticatedUser);
        model.addAttribute("interval", optionalSettings.map(Settings::getIntervalMinutes).orElse(0));
    }

    private void addUsersListAttributes(Model model) {
        model.addAttribute("users", userService.findAll());
    }
}

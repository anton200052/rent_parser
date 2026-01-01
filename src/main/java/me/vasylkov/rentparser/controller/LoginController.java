package me.vasylkov.rentparser.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login-form")
    public String loginForm(Model model) {
        return "auth/login";
    }
}

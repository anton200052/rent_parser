package me.vasylkov.rentparser.component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class HtmxChecker {
    public boolean isHtmx(HttpServletRequest req) {
        return "true".equalsIgnoreCase(req.getHeader("HX-Request"));
    }
}

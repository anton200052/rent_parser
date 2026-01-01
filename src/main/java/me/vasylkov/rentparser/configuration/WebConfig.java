package me.vasylkov.rentparser.configuration;

import lombok.RequiredArgsConstructor;
import me.vasylkov.rentparser.component.StringToRoleConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final StringToRoleConverter stringToRoleConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToRoleConverter);
    }
}

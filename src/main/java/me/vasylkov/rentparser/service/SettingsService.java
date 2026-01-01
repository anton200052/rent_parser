package me.vasylkov.rentparser.service;

import me.vasylkov.rentparser.entity.Settings;
import me.vasylkov.rentparser.entity.User;

import java.util.Optional;

public interface SettingsService {
    Optional<Settings> getSettingsByUser(User user);
    Settings updateSettingsFromForm(Settings newSettings, User user);
    Settings createDefaultSettings(User user);
}

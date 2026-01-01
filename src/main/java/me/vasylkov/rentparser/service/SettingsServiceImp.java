package me.vasylkov.rentparser.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.vasylkov.rentparser.entity.Settings;
import me.vasylkov.rentparser.entity.User;
import me.vasylkov.rentparser.repository.SettingsRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettingsServiceImp implements SettingsService {
    private final SettingsRepository repo;

    @Override
    public Optional<Settings> getSettingsByUser(User user) {
        return repo.findByUser(user);
    }

    @Override
    @Transactional
    public Settings updateSettingsFromForm(Settings newSettings, User user) {
        Optional<Settings> optionalSettings = repo.findByUser(user);
        if (optionalSettings.isPresent()) {
            Settings existedSettings = optionalSettings.get();

            existedSettings.setWorkFrom(newSettings.getWorkFrom());
            existedSettings.setWorkUntil(newSettings.getWorkUntil());
            existedSettings.setIntervalMinutes(newSettings.getIntervalMinutes());

            return repo.save(existedSettings);
        }
        return null;
    }

    @Override
    public Settings createDefaultSettings(User user) {
        Settings newSettings = new Settings();
        newSettings.setUser(user);
        newSettings.setWorkFrom("09:00:00");
        newSettings.setWorkUntil("18:00:00");
        newSettings.setIntervalMinutes(5);
        return newSettings;
    }
}

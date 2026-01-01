package me.vasylkov.rentparser.service;

import jakarta.transaction.Transactional;
import me.vasylkov.rentparser.component.SessionsManager;
import me.vasylkov.rentparser.entity.Role;
import me.vasylkov.rentparser.entity.Settings;
import me.vasylkov.rentparser.entity.User;
import me.vasylkov.rentparser.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;
    @Qualifier("settingsServiceImp")
    private final SettingsService settingsService;
    private final PasswordEncoder passwordEncoder;
    private final SessionsManager sessionsManager;

    public UserServiceImp(UserRepository userRepository, SettingsService settingsService, PasswordEncoder passwordEncoder, SessionsManager sessionsManager) {
        this.userRepository = userRepository;
        this.settingsService = settingsService;
        this.passwordEncoder = passwordEncoder;
        this.sessionsManager = sessionsManager;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User createFromForm(User user) {
        Settings defSettings = settingsService.createDefaultSettings(user);

        if (user.getUsername() == null || user.getUsername().isEmpty() || user.getPassword() == null || user.getPassword().isEmpty() || user.getRole() == null) {
            return null;
        }
        user.setId(null);
        user.setSettings(defSettings);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User editFromForm(User user) {
        Optional<User> optionalUser = userRepository.findById(user.getId());
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            String userName = user.getUsername();
            String password = user.getPassword();
            Role role = user.getRole();

            if (userName != null && !userName.isEmpty()) {
                existingUser.setUsername(user.getUsername());
            }

            if (password != null && !password.isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            if (role != null) {
                existingUser.setRole(user.getRole());
            }

            return userRepository.save(existingUser);
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            sessionsManager.closeUserSession(user);
            userRepository.deleteById(id);
        }
    }

    @Override
    @Transactional
    public void toggleEnabled(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            boolean isEnabled = user.isEnabled();
            if (isEnabled) {
                user.setEnabled(false);
                sessionsManager.closeUserSession(user);
            } else {
                user.setEnabled(true);
            }
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public void updateLastScheduledRun(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setLastScheduledRun(String.valueOf(LocalDateTime.now()));
            userRepository.save(user);
        }
    }
}

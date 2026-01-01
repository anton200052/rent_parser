package me.vasylkov.rentparser.service;

import me.vasylkov.rentparser.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    User save(User user);
    User createFromForm(User user);
    User editFromForm(User user);
    List<User> findAll();
    void deleteById(Long id);
    void toggleEnabled(Long id);
    void updateLastScheduledRun(Long id);
}

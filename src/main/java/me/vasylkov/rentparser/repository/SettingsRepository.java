package me.vasylkov.rentparser.repository;

import me.vasylkov.rentparser.entity.Settings;
import me.vasylkov.rentparser.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Integer> {
    Optional<Settings> findByUser(User user);

}

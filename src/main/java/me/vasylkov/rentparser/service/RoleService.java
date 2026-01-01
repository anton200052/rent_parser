package me.vasylkov.rentparser.service;

import me.vasylkov.rentparser.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    Optional<Role> findById(Long id);
    Optional<Role> findByName(String name);
    Role save(Role role);
    List<Role> findAll();
}

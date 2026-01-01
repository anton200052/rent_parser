package me.vasylkov.rentparser.component;

import jakarta.transaction.Transactional;
import me.vasylkov.rentparser.entity.Role;
import me.vasylkov.rentparser.entity.User;
import me.vasylkov.rentparser.service.RoleService;
import me.vasylkov.rentparser.service.SettingsService;
import me.vasylkov.rentparser.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    @Qualifier("roleServiceImp")
    private final RoleService roleService;
    @Qualifier("userServiceImp")
    private final UserService userService;
    @Qualifier("settingsServiceImp")
    private final SettingsService settingsService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleService roleService, UserService userService, SettingsService settingsService, PasswordEncoder passwordEncoder) {
        this.roleService = roleService;
        this.userService = userService;
        this.settingsService = settingsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        createUserRoleIfNotExist();
        Role adminRole = getAdminRole();

        User admin = userService.findByUsername("admin")
                .orElseGet(() -> createAndSaveUser(adminRole));

        if (!admin.isEnabled()) {
            admin.setEnabled(true);
        }
    }

    private User createAndSaveUser(Role adminRole) {
        User admin = new User();
        admin.setSettings(settingsService.createDefaultSettings(admin));
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setRole(adminRole);
        admin.setEnabled(true);
        return userService.save(admin);
    }

    private Role getAdminRole() {
        return roleService.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ROLE_ADMIN");
                    return roleService.save(newRole);
                });
    }

    private void createUserRoleIfNotExist() {
        if (roleService.findByName("ROLE_USER").isEmpty()) {
            Role newRole = new Role();
            newRole.setName("ROLE_USER");
            roleService.save(newRole);
        }
    }
}

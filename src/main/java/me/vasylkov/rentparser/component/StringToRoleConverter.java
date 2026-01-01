package me.vasylkov.rentparser.component;

import me.vasylkov.rentparser.entity.Role;
import me.vasylkov.rentparser.service.RoleService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToRoleConverter implements Converter<String, Role> {
    @Qualifier("roleServiceImp")
    private final RoleService roleService;

    public StringToRoleConverter(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public Role convert(String source) {
        if (source.isEmpty()) {
            return null;
        }

        try {
            Long id = Long.parseLong(source);
            return roleService.findById(id).orElse(null);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }
}

package com.example.auth_service.common.exceptions;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.auth_service.user.entities.Role.RoleName;

public class RolesNotFoundException extends RuntimeException {
    public RolesNotFoundException(Set<RoleName> roleNames, Set<RoleName> foundRoleNames) {
        super("The following roles were not found: " +
                roleNames.stream()
                        .filter(role -> !foundRoleNames.contains(role))
                        .map(Enum::name)
                        .collect(Collectors.joining(", ")));
    }

}

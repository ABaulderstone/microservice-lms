package com.example.auth_service.common;

import java.util.Set;

public record UserContext(String userId, Set<String> roles) {
    public boolean hasRole(String role) {
        return roles.contains(role) || roles.contains("ROLE_" + role);
    }
}

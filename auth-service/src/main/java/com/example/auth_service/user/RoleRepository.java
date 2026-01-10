package com.example.auth_service.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth_service.user.entities.Role;
import com.example.auth_service.user.entities.Role.RoleName;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
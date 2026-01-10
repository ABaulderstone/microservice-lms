package com.example.auth_service.seeder;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.auth_service.user.RoleRepository;
import com.example.auth_service.user.UserService;
import com.example.auth_service.user.entities.Role;

@Profile("dev")
@Component
public class UserSeeder implements CommandLineRunner {
    private final UserService userService;
    private final RoleRepository roleRepository;

    public UserSeeder(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Seeding roles...");
        if (this.roleRepository.count() == 0) {
            for (var roleName : Role.RoleName.values()) {
                var role = new com.example.auth_service.user.entities.Role();
                role.setName(roleName);
                roleRepository.save(role);
            }

        }
        System.out.println("Seeding default users...");
        if (userService.findByEmail("admin@test.com").isEmpty()) {
            userService.createUser("admin@test.com", "password",
                    Set.of(roleRepository.findByName(Role.RoleName.ADMIN).get()));
        }
    }
}

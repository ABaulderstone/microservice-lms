package com.example.auth_service.seeder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.auth_service.user.UserService;

@Profile("dev")
@Component
public class UserSeeder implements CommandLineRunner {
    private final UserService userService;

    public UserSeeder(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Seeding default users...");
        if (userService.findByEmail("admin@test.com").isEmpty()) {
            userService.createUser("admin@test.com", "password");
        }
    }
}

package com.example.auth_service.user;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.auth_service.auth.PasswordEncodingService;
import com.example.auth_service.user.entities.Role;
import com.example.auth_service.user.entities.User;
import com.example.auth_service.user.entities.Role.RoleName;
import com.example.auth_service.user.kafka.UserEventPublisher;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncodingService passwordEncodingService;
    private final UserEventPublisher userEventPublisher;
    private final RoleRepository roleRepository;

    public UserService(
            UserRepository userRepository,
            PasswordEncodingService passwordEncodingService,
            UserEventPublisher userEventPublisher,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncodingService = passwordEncodingService;
        this.userEventPublisher = userEventPublisher;
        this.roleRepository = roleRepository;
    }

    public User createUser(String email, String rawPassword, Set<RoleName> roleNames) {
        String hashedPassword = passwordEncodingService.hashPassword(rawPassword);
        User newUser = new User();
        Set<Role> roles = roleRepository.findByNameIn(roleNames);

        if (roles.size() != roleNames.size()) {
            throw new IllegalStateException("Some roles not found");
        }

        newUser.setEmail(email);
        newUser.setPassword(hashedPassword);
        newUser.setRoles(roles);
        User saved = userRepository.save(newUser);
        System.out.println("User created with ID: " + saved.getId());
        userEventPublisher.publishUserCreatedEvent(saved.getId());
        return saved;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

}

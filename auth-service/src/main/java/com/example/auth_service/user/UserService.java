package com.example.auth_service.user;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.auth_service.auth.PasswordEncodingService;
import com.example.auth_service.user.entities.User;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncodingService passwordEncodingService;

    public UserService(UserRepository userRepository, PasswordEncodingService passwordEncodingService) {
        this.userRepository = userRepository;
        this.passwordEncodingService = passwordEncodingService;
    }

    public User createUser(String email, String rawPassword) {
        String hashedPassword = passwordEncodingService.hashPassword(rawPassword);
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(hashedPassword);
        return userRepository.save(newUser);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

}

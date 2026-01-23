package com.example.auth_service.user;

import com.example.auth_service.auth.PasswordEncodingService;
import com.example.auth_service.common.exceptions.RolesNotFoundException;
import com.example.auth_service.common.exceptions.UserAlreadyExistsException;
import com.example.auth_service.user.entities.Role;
import com.example.auth_service.user.entities.Role.RoleName;
import com.example.auth_service.user.entities.User;
import com.example.auth_service.user.kafka.UserEventPublisher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncodingService passwordEncodingService;

    @Mock
    private UserEventPublisher userEventPublisher;

    @Mock
    private RoleRepository roleRepository;

    @Spy
    @InjectMocks
    private UserService userService;

    @Test
    void createUser_whenEmailAlreadyExists_throwsUserAlreadyExistsException() {
        // Given
        String email = "existing@test.com";
        String rawPassword = "password123";
        Set<RoleName> roleNames = Set.of(RoleName.CANDIDATE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        // When / Then
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(email, rawPassword, roleNames);
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_whenRolesNotFound_throwsRolesNotFoundException() {
        // Given
        String email = "test@test.com";
        String rawPassword = "password123";
        Set<RoleName> roleNames = Set.of(RoleName.CANDIDATE, RoleName.ADMIN);
        String hashedPassword = "hashedPassword123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncodingService.hashPassword(rawPassword)).thenReturn(hashedPassword);
        when(roleRepository.findByNameIn(roleNames)).thenReturn(Set.of(new Role())); // Only one role found
        // Then
        assertThrows(RolesNotFoundException.class, () -> {
            userService.createUser(email, rawPassword, roleNames);
        });
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_whenValidInputs_createsUserSuccessfully() {
        // Given
        String email = "test@test.com";
        String rawPassword = "password123";
        Set<RoleName> roleNames = Set.of(RoleName.CANDIDATE);
        String hashedPassword = "hashedPassword123";
        Role candidateRole = new Role();
        candidateRole.setName(RoleName.CANDIDATE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncodingService.hashPassword(rawPassword)).thenReturn(hashedPassword);
        when(roleRepository.findByNameIn(roleNames)).thenReturn(Set.of(candidateRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate generated ID
            return user;
        });
        // Then
        User createdUser = userService.createUser(email, rawPassword, roleNames);
        assertNotNull(createdUser);
        assertEquals(1L, createdUser.getId());
        assertEquals(email, createdUser.getEmail());
        assertEquals(hashedPassword, createdUser.getPassword());
        assertTrue(createdUser.getRoles().contains(candidateRole));
        verify(userEventPublisher, times(1)).publishUserCreatedEvent(1L);

    }

    @Test
    void findByEmail_whenUserExists_returnsUser() {
        // Given
        String email = "test@test.com";
        User mockUser = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        // When
        Optional<User> result = userService.findByEmail(email);
        // Then
        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
    }

    @Test
    void findByEmail_whenUserDoesNotExist_returnsEmptyOptional() {
        // Given
        String email = "test@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        // When
        Optional<User> result = userService.findByEmail(email);
        // Then
        assertFalse(result.isPresent());

    }

    @Test
    void findById_whenUserExists_returnsUser() {
        // Given
        Long userId = 1L;
        User mockUser = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        // When
        Optional<User> result = userService.findById(userId);
        // Then
        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
    }

    @Test
    void findById_whenUserDoesNotExist_returnsEmptyOptional() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        // When
        Optional<User> result = userService.findById(userId);
        // Then
        assertFalse(result.isPresent());
    }

}
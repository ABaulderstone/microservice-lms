package com.example.auth_service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.auth_service.common.exceptions.RolesNotFoundException;
import com.example.auth_service.common.exceptions.UserAlreadyExistsException;
import com.example.auth_service.user.entities.Role;
import com.example.auth_service.user.entities.Role.RoleName;
import com.example.auth_service.user.entities.User;
import com.example.auth_service.user.grpc.UserGrpcService;
import com.example.user.proto.v1.CreateUserRequest;
import com.example.user.proto.v1.UserRequest;
import com.example.user.proto.v1.UserResponse;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

@ExtendWith(MockitoExtension.class)
public class UserGrpcServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private StreamObserver<UserResponse> responseObserver;

    @Spy
    @InjectMocks
    private UserGrpcService userGrpcService;

    @Captor
    private ArgumentCaptor<UserResponse> responseCaptor;

    @Captor
    private ArgumentCaptor<StatusRuntimeException> errorCaptor;

    @Test
    public void findUser_whenUserExists_returnsUserResponse() {
        // Given
        Long userId = 1L;
        User mockUser = createMockUser(userId, "test@test.com", Set.of(RoleName.CANDIDATE));

        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));

        UserRequest request = UserRequest.newBuilder()
                .setUserId(userId)
                .build();

        // When
        userGrpcService.findUser(request, responseObserver);

        // Then
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());

        UserResponse response = responseCaptor.getValue();
        assertEquals(userId, response.getId());
        assertEquals("test@test.com", response.getEmail());
        assertEquals(1, response.getRolesCount());
        assertEquals("CANDIDATE", response.getRoles(0));
    }

    @Test
    public void findUser_whenUserNotFound_returnsNotFoundError() {
        // Given
        Long userId = 999L;
        when(userService.findById(userId)).thenReturn(Optional.empty());

        UserRequest request = UserRequest.newBuilder()
                .setUserId(userId)
                .build();

        // When
        userGrpcService.findUser(request, responseObserver);

        // Then
        verify(responseObserver).onError(errorCaptor.capture());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();

        StatusRuntimeException error = errorCaptor.getValue();
        assertEquals(Status.Code.NOT_FOUND, error.getStatus().getCode());
        assertTrue(error.getMessage().contains("User with ID 999 not found"));
    }

    @Test
    public void findUser_whenUserHasMultipleRoles_returnsAllRoles() {
        // Given
        Long userId = 1L;
        User mockUser = createMockUser(
                userId,
                "admin@test.com",
                Set.of(RoleName.ADMIN, RoleName.TALENT));

        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));

        UserRequest request = UserRequest.newBuilder()
                .setUserId(userId)
                .build();

        // When
        userGrpcService.findUser(request, responseObserver);

        // Then
        verify(responseObserver).onNext(responseCaptor.capture());

        UserResponse response = responseCaptor.getValue();
        assertEquals(2, response.getRolesCount());
        assertTrue(response.getRolesList().contains("ADMIN"));
        assertTrue(response.getRolesList().contains("TALENT"));
    }

    @Test
    public void createUser_whenValidRequest_createsUserSuccessfully() {
        // Given
        String email = "newuser@test.com";
        User mockUser = createMockUser(1L, email, Set.of(RoleName.CANDIDATE));

        when(userService.createUser(
                eq(email),
                eq("password123"),
                eq(Set.of(RoleName.CANDIDATE)))).thenReturn(mockUser);

        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setEmail(email)
                .addRoles("CANDIDATE")
                .build();

        // When
        userGrpcService.createUser(request, responseObserver);

        // Then
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());

        UserResponse response = responseCaptor.getValue();
        assertEquals(1L, response.getId());
        assertEquals(email, response.getEmail());
        assertEquals(1, response.getRolesCount());
        assertEquals("CANDIDATE", response.getRoles(0));
    }

    @Test
    public void createUser_whenMultipleRoles_createsUserWithAllRoles() {
        // Given
        String email = "admin@test.com";
        Set<RoleName> roleNames = Set.of(RoleName.ADMIN, RoleName.TALENT);
        User mockUser = createMockUser(1L, email, roleNames);

        when(userService.createUser(
                eq(email),
                eq("password123"),
                eq(roleNames))).thenReturn(mockUser);

        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setEmail(email)
                .addRoles("ADMIN")
                .addRoles("TALENT")
                .build();

        // When
        userGrpcService.createUser(request, responseObserver);

        // Then
        verify(responseObserver).onNext(responseCaptor.capture());

        UserResponse response = responseCaptor.getValue();
        assertEquals(2, response.getRolesCount());
        assertTrue(response.getRolesList().contains("ADMIN"));
        assertTrue(response.getRolesList().contains("TALENT"));
    }

    @Test
    public void createUser_whenUserAlreadyExists_returnsAlreadyExistsError() {
        // Given
        String email = "existing@test.com";

        when(userService.createUser(any(), any(), any()))
                .thenThrow(new UserAlreadyExistsException(email));

        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setEmail(email)
                .addRoles("CANDIDATE")
                .build();

        // When
        userGrpcService.createUser(request, responseObserver);

        // Then
        verify(responseObserver).onError(errorCaptor.capture());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();

        StatusRuntimeException error = errorCaptor.getValue();
        assertEquals(Status.Code.ALREADY_EXISTS, error.getStatus().getCode());
        assertTrue(error.getMessage().contains(email));
    }

    @Test
    public void createUser_whenRolesNotFound_returnsNotFoundError() {
        // Given
        String email = "test@test.com";
        Set<RoleName> requestedRoles = Set.of(RoleName.CANDIDATE, RoleName.ADMIN);
        Set<RoleName> foundRoles = Set.of(RoleName.CANDIDATE);

        when(userService.createUser(any(), any(), any()))
                .thenThrow(new RolesNotFoundException(requestedRoles, foundRoles));

        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setEmail(email)
                .addRoles("CANDIDATE")
                .addRoles("ADMIN")
                .build();

        // When
        userGrpcService.createUser(request, responseObserver);

        // Then
        verify(responseObserver).onError(errorCaptor.capture());

        StatusRuntimeException error = errorCaptor.getValue();
        assertEquals(Status.Code.NOT_FOUND, error.getStatus().getCode());
    }

    @Test
    public void createUser_whenInvalidRoleName_returnsInvalidArgumentError() {
        // Given
        String email = "test@test.com";

        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setEmail(email)
                .addRoles("INVALID_ROLE")
                .build();

        // When
        userGrpcService.createUser(request, responseObserver);

        // Then
        verify(responseObserver).onError(errorCaptor.capture());

        StatusRuntimeException error = errorCaptor.getValue();
        assertEquals(Status.Code.INVALID_ARGUMENT, error.getStatus().getCode());

        // Assert on the actual error message from valueOf()
        assertTrue(error.getMessage().contains("No enum constant"));
        assertTrue(error.getMessage().contains("INVALID_ROLE"));

        // Verify the service was never called
        verify(userService, never()).createUser(any(), any(), any());
    }

    @Test
    public void createUser_whenUnexpectedException_returnsInternalError() {
        // Given
        String email = "test@test.com";

        when(userService.createUser(any(), any(), any()))
                .thenThrow(new RuntimeException("Database connection failed"));

        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setEmail(email)
                .addRoles("CANDIDATE")
                .build();

        // When
        userGrpcService.createUser(request, responseObserver);

        // Then
        verify(responseObserver).onError(errorCaptor.capture());

        StatusRuntimeException error = errorCaptor.getValue();
        assertEquals(Status.Code.INTERNAL, error.getStatus().getCode());
        assertEquals("Internal server error", error.getStatus().getDescription());

    }

    @Test
    public void createUser_verifyDefaultPasswordIsUsed() {
        // Given
        String email = "test@test.com";
        User mockUser = createMockUser(1L, email, Set.of(RoleName.CANDIDATE));

        when(userService.createUser(any(), any(), any())).thenReturn(mockUser);

        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setEmail(email)
                .addRoles("CANDIDATE")
                .build();

        // When
        userGrpcService.createUser(request, responseObserver);

        verify(userService).createUser(
                eq(email),
                eq("password123"), // Verify default password
                any());
    }

    private User createMockUser(Long id, String email, Set<RoleName> roleNames) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);

        Set<Role> roles = roleNames.stream()
                .map(roleName -> {
                    Role role = new Role();
                    role.setName(roleName);
                    return role;
                })
                .collect(Collectors.toSet());

        user.setRoles(roles);
        return user;
    }
}

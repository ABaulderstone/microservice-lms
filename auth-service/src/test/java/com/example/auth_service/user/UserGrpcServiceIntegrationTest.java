package com.example.auth_service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.auth_service.user.kafka.UserEventPublisher;
import com.example.user.proto.v1.CreateUserRequest;
import com.example.user.proto.v1.UserResponse;
import com.example.user.proto.v1.UserServiceGrpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Tag("integration")
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class UserGrpcServiceIT {

        @Container
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                        .withDatabaseName("test_auth_db")
                        .withUsername("test")
                        .withPassword("test");

        @DynamicPropertySource
        static void datasourceProps(DynamicPropertyRegistry registry) {
                registry.add("spring.datasource.url", postgres::getJdbcUrl);
                registry.add("spring.datasource.username", postgres::getUsername);
                registry.add("spring.datasource.password", postgres::getPassword);
        }

        @GrpcClient("test")
        private UserServiceGrpc.UserServiceBlockingStub userStub;

        @Autowired
        private UserRepository userRepository;

        @MockBean
        private UserEventPublisher userEventPublisher; // disable Kafka side-effects

        @BeforeEach
        void cleanDb() {
                userRepository.deleteAll();
        }

        @Test
        void createUser_success_persistsUser() {
                CreateUserRequest request = CreateUserRequest.newBuilder()
                                .setEmail("integration@test.com")
                                .addRoles("CANDIDATE")
                                .build();

                UserResponse response = userStub.createUser(request);

                assertThat(response.getId()).isPositive();
                assertThat(response.getEmail()).isEqualTo("integration@test.com");
                assertThat(response.getRolesList()).contains("CANDIDATE");

                assertThat(userRepository.findById(response.getId())).isPresent();
        }

        @Test
        void createUser_duplicateEmail_returnsAlreadyExists() {
                userStub.createUser(
                                CreateUserRequest.newBuilder()
                                                .setEmail("dup@test.com")
                                                .addRoles("CANDIDATE")
                                                .build());

                StatusRuntimeException ex = assertThrows(
                                StatusRuntimeException.class,
                                () -> userStub.createUser(
                                                CreateUserRequest.newBuilder()
                                                                .setEmail("dup@test.com")
                                                                .addRoles("ADMIN")
                                                                .build()));

                assertThat(ex.getStatus().getCode())
                                .isEqualTo(Status.Code.ALREADY_EXISTS);
        }
}

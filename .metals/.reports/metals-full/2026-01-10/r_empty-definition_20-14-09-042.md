error id: file://<WORKSPACE>/auth-service/src/main/java/com/example/auth_service/user/grpc/UserGrpcService.java:
file://<WORKSPACE>/auth-service/src/main/java/com/example/auth_service/user/grpc/UserGrpcService.java
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 2034
uri: file://<WORKSPACE>/auth-service/src/main/java/com/example/auth_service/user/grpc/UserGrpcService.java
text:
```scala
package com.example.auth_service.user.grpc;

import java.util.Set;

import com.example.auth_service.common.decorators.RequireAnyRole;
import com.example.auth_service.user.UserService;
import com.example.auth_service.user.entities.Role;
import com.example.auth_service.user.entities.User;
import com.example.user.proto.v1.UserRequest;
import com.example.user.proto.v1.UserResponse;
import com.example.user.proto.v1.UserServiceGrpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {
    private final UserService userService;

    public UserGrpcService(UserService userService) {
        this.userService = userService;
    }

    @Override
    @RequireAnyRole({ "TALENT", "ADMIN" })
    public void findUser(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        long userId = request.getUserId();
        User foundUser = userService.findById(userId).orElse(null);

        if (foundUser == null) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("User with ID " + userId + " not found")
                    .asRuntimeException());
            return;
        }

        UserResponse response = UserResponse.newBuilder()
                .setId(foundUser.getId())
                .setEmail(foundUser.getEmail())
                .addAllRoles(foundUser.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toList())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

@Override
@RequireAnyRole({ "ADMIN" })
public void createUser(com.example.user.proto.v1.CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
    String email = request.getEmail();
    var roles = request.getRolesList(); // Assuming roles are sent as a list of strings
    Set<Role> roleSet = roles.stream()
            .map(Role::@@valueOf)
            .collect(Collectors.toSet());
    User newUser = userService.createUser(email, roleSet);
    
    UserResponse response = UserResponse.newBuilder()
            .setId(newUser.getId())
            .setEmail(newUser.getEmail())
            .addAllRoles(newUser.getRoles().stream()
                    .map(role -> role.getName().name())
                    .toList())
            .build();
    
    responseObserver.onNext(response);
    responseObserver.onCompleted();
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 
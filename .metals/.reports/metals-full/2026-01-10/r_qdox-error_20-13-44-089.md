error id: file://<WORKSPACE>/auth-service/src/main/java/com/example/auth_service/user/grpc/UserGrpcService.java
file://<WORKSPACE>/auth-service/src/main/java/com/example/auth_service/user/grpc/UserGrpcService.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[67,1]

error in qdox parser
file content:
```java
offset: 2450
uri: file://<WORKSPACE>/auth-service/src/main/java/com/example/auth_service/user/grpc/UserGrpcService.java
text:
```scala
package com.example.auth_service.user.grpc;

import com.example.auth_service.common.decorators.RequireAnyRole;
import com.example.auth_service.user.UserService;
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
            .map(Role::valueOf)
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
@@
```

```



#### Error stacktrace:

```
com.thoughtworks.qdox.parser.impl.Parser.yyerror(Parser.java:2025)
	com.thoughtworks.qdox.parser.impl.Parser.yyparse(Parser.java:2147)
	com.thoughtworks.qdox.parser.impl.Parser.parse(Parser.java:2006)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:232)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:190)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:94)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:89)
	com.thoughtworks.qdox.library.SortedClassLibraryBuilder.addSource(SortedClassLibraryBuilder.java:162)
	com.thoughtworks.qdox.JavaProjectBuilder.addSource(JavaProjectBuilder.java:174)
	scala.meta.internal.mtags.JavaMtags.indexRoot(JavaMtags.scala:49)
	scala.meta.internal.metals.SemanticdbDefinition$.foreachWithReturnMtags(SemanticdbDefinition.scala:99)
	scala.meta.internal.metals.Indexer.indexSourceFile(Indexer.scala:546)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3(Indexer.scala:677)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3$adapted(Indexer.scala:674)
	scala.collection.IterableOnceOps.foreach(IterableOnce.scala:630)
	scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:628)
	scala.collection.AbstractIterator.foreach(Iterator.scala:1313)
	scala.meta.internal.metals.Indexer.reindexWorkspaceSources(Indexer.scala:674)
	scala.meta.internal.metals.MetalsLspService.$anonfun$onChange$2(MetalsLspService.scala:912)
	scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
	scala.concurrent.Future$.$anonfun$apply$1(Future.scala:691)
	scala.concurrent.impl.Promise$Transformation.run(Promise.scala:500)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	java.base/java.lang.Thread.run(Thread.java:833)
```
#### Short summary: 

QDox parse error in file://<WORKSPACE>/auth-service/src/main/java/com/example/auth_service/user/grpc/UserGrpcService.java
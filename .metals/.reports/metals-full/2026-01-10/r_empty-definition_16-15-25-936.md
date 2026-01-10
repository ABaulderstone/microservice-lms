error id: file://<WORKSPACE>/auth-service/src/main/java/com/example/auth_service/common/decorators/RequireAnyRoleAspect.java:com/example/auth_service/common/RequestContext#
file://<WORKSPACE>/auth-service/src/main/java/com/example/auth_service/common/decorators/RequireAnyRoleAspect.java
empty definition using pc, found symbol in pc: com/example/auth_service/common/RequestContext#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 242
uri: file://<WORKSPACE>/auth-service/src/main/java/com/example/auth_service/common/decorators/RequireAnyRoleAspect.java
text:
```scala
package com.example.auth_service.common.decorators;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.example.auth_service.common.RequestContext@@;
import com.example.auth_service.common.UserContext;
import com.example.auth_service.common.exceptions.ForbiddenException;

@Aspect
@Component
public class RequireAnyRoleAspect {
    @Before("@annotation(requireAnyRole)")
    public void checkRoles(RequireAnyRole requireAnyRole) {
        UserContext userContext = RequestContext.getUserContext();

        if (userContext == null) {
            throw new ForbiddenException("No user context found");
        }

        for (String role : requireAnyRole.value()) {
            if (userContext.hasRole(role)) {
                return; // User has at least one required role
            }
        }

        throw new ForbiddenException("User does not have any of the required roles");
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: com/example/auth_service/common/RequestContext#
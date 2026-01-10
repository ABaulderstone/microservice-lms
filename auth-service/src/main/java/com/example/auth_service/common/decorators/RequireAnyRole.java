package com.example.auth_service.common.decorators;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RequireAnyRole {
    String[] value();
}

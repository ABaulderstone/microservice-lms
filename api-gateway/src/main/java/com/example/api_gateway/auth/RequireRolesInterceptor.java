package com.example.api_gateway.auth;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequireRolesInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        RequireRoles requireRoles = null;
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        } else {
            requireRoles = handlerMethod.getMethodAnnotation(RequireRoles.class);

        }
        if (requireRoles != null) {
            checkRoles(requireRoles.value());
        }
        return true;
    }

    private void checkRoles(String[] requiredRoles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new InsufficientAuthenticationException("Authentication Required");
        }
        Set<String> authRoles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        boolean allowed = Arrays.stream(requiredRoles).anyMatch(r -> authRoles.contains("ROLE_" + r));
        if (!allowed) {
            throw new AccessDeniedException("User does not have required role");
        }
    }

}

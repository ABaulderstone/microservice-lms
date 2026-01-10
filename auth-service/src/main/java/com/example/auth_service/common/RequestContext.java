package com.example.auth_service.common;

public final class RequestContext {
    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    private RequestContext() {
    }

    public static void setUserContext(UserContext userContext) {
        CONTEXT.set(userContext);
    }

    public static UserContext getUserContext() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

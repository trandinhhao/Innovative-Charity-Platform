package dev.lhs.charity_backend.constant;

public class PublicEndpoint {

    public static final String[] PUBLIC_ENDPOINTS = {
            "/users",
            "/auth/token",
            "/auth/introspect",
            "/auth/logout",
            "/auth/refresh"
    };

    private PublicEndpoint() {};

}

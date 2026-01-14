package dev.lhs.charity_backend.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class SecurityUtils {

    private SecurityUtils() {}

    public static Jwt currentJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) return null;

        return ((JwtAuthenticationToken) auth).getToken();
    }

    public static Long getUserId() {
        Jwt jwt = currentJwt();
        return jwt != null ? currentJwt().getClaim("userId") : null;
    }

    public static String getUsername() {
        Jwt jwt = currentJwt();
        return jwt != null ? jwt.getSubject() : null;
    }
}

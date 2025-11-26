package com.aleksa.banking_api.security;

public class SecurityConstants {

    public static final String SIGN_UP_URLS = "api/v1/user/**";
    public static final String SECRET = "SecretKeyToGenJWTs";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    public static final int EXPIRATION_TIME_IN_MINUTES = 15;

    public static final String[] PUBLIC_MATCHERS = {
            "/",                          // home
            "/actuator/health",           // health check
            "/v3/api-docs/**",            // Swagger docs
            "/swagger-ui/**",             // Swagger UI
            "/swagger-ui.html",
            "/error",                     // Spring Boot error page
            "/favicon.ico",               // favicon
            "/css/**",                    // static resources
            "/js/**",
            "/images/**"
    };
}

package com.coworking.reservations.config.security;

public final class SecurityAuthorities {

    private SecurityAuthorities() {
    }

    public static final String ADMIN = "hasRole('ADMIN')";
    public static final String USER = "hasRole('USER')";
    public static final String USER_OR_ADMIN =
            "hasAnyRole('USER', 'ADMIN')";
}
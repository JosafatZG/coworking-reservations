package com.coworking.reservations.auth.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn
) {
}
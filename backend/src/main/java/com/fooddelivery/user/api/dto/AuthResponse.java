package com.fooddelivery.user.api.dto;

public record AuthResponse(
    String token,
    String email,
    String role
) {}
package com.fooddelivery.restaurant.api.dto;

import com.fooddelivery.restaurant.domain.Restaurant;
import java.util.UUID;

public record RestaurantResponse(
    UUID id,
    String name,
    String addressLine,
    String status,
    Integer prepTimeMinutes
) {
    public static RestaurantResponse fromEntity(Restaurant restaurant) {
        return new RestaurantResponse(
            restaurant.getId(),
            restaurant.getName(),
            restaurant.getAddressLine(),
            restaurant.getStatus().name(),
            restaurant.getPrepTimeMinutes()
        );
    }
}
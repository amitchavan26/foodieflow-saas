package com.fooddelivery.restaurant.api.dto;

import com.fooddelivery.restaurant.domain.MenuItem;
import java.util.UUID;

public record MenuItemResponse(
    UUID id,
    String name,
    String description,
    Long priceInCents,
    Boolean isAvailable
) {
    public static MenuItemResponse fromEntity(MenuItem item) {
        return new MenuItemResponse(
            item.getId(),
            item.getName(),
            item.getDescription(),
            item.getPriceInCents(),
            item.isAvailable()
        );
    }
}
package com.fooddelivery.restaurant.api;

import com.fooddelivery.restaurant.api.dto.MenuItemResponse;
import com.fooddelivery.restaurant.infrastructure.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemRepository menuItemRepository;

    @GetMapping("/{restaurantId}/menu-items")
    public List<MenuItemResponse> getMenuItems(@PathVariable UUID restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId).stream()
            // Use isAvailable() instead of getIsAvailable() for Lombok booleans
            .filter(item -> item.isAvailable()) 
            .map(MenuItemResponse::fromEntity)
            .collect(Collectors.toList());
    }
}
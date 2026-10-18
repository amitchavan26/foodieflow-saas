package com.fooddelivery.restaurant.api;

import com.fooddelivery.restaurant.api.dto.RestaurantResponse;
import com.fooddelivery.restaurant.infrastructure.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;

    @GetMapping
    public List<RestaurantResponse> getAllRestaurants() {
        // For V1, we return all active restaurants. 
        // In Layer 8, we will add geospatial filtering (e.g., "within 5km of user").
        return restaurantRepository.findAll().stream()
            .map(RestaurantResponse::fromEntity)
            .collect(Collectors.toList());
    }
}
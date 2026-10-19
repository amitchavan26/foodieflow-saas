package com.fooddelivery.restaurant.api;

import com.fooddelivery.restaurant.domain.Restaurant;
import com.fooddelivery.restaurant.api.dto.RestaurantResponse;
import com.fooddelivery.restaurant.infrastructure.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;

    @GetMapping
    public List<RestaurantResponse> getRestaurants(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "5000") Double radiusMeters) {
        
        List<Restaurant> restaurants;
        
        // If lat and lng are provided, use the geospatial query
        if (lat != null && lng != null) {
            restaurants = restaurantRepository.findNearbyRestaurants(lat, lng, radiusMeters);
        } else {
            // Fallback to all active restaurants
            restaurants = restaurantRepository.findAll().stream()
                .filter(r -> "ACTIVE".equals(r.getStatus().name())) // Adjust if your enum is different
                .toList();
        }

        return restaurants.stream()
            .map(RestaurantResponse::fromEntity)
            .collect(Collectors.toList());
    }
}
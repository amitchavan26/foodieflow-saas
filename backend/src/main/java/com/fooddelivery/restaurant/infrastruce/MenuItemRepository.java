package com.fooddelivery.restaurant.infrastructure;

import com.fooddelivery.restaurant.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // <-- This is the missing import!
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findByRestaurantId(UUID restaurantId);
}
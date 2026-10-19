package com.fooddelivery.restaurant.infrastructure;

import com.fooddelivery.restaurant.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    
    // Find all active restaurants within a given radius (in meters) of a lat/lng point
    @Query(value = """
        SELECT r.* FROM restaurants r 
        WHERE r.status = 'ACTIVE' 
        AND ST_DWithin(r.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radiusMeters)
        ORDER BY ST_Distance(r.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography) ASC
        """, nativeQuery = true)
    List<Restaurant> findNearbyRestaurants(
        @Param("lat") double latitude,
        @Param("lng") double longitude,
        @Param("radiusMeters") double radiusMeters
    );
}
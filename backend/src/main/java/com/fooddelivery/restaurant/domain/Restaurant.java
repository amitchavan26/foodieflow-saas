package com.fooddelivery.restaurant.domain;

import java.math.BigDecimal;
import java.time.Instant;

import com.fooddelivery.common.persistence.BaseAuditedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
public class Restaurant extends BaseAuditedEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "address_line", nullable = false, length = 500)
    private String addressLine;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RestaurantStatus status = RestaurantStatus.PENDING_APPROVAL;

    @Column(nullable = false)
    private boolean paused;

    @Column(name = "prep_time_minutes", nullable = false)
    private int prepTimeMinutes;

    @Column(name = "delivery_radius_meters", nullable = false)
    private int deliveryRadiusMeters;

    @Column(name = "deleted_at")
    private Instant deletedAt;


    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public RestaurantStatus getStatus() {
        return status;
    }

    public boolean isPaused() {
        return paused;
    }

    public int getPrepTimeMinutes() {
        return prepTimeMinutes;
    }

    public int getDeliveryRadiusMeters() {
        return deliveryRadiusMeters;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}

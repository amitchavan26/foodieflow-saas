package com.fooddelivery.restaurant.domain;

import java.time.Instant;
import java.util.UUID;

import com.fooddelivery.common.persistence.BaseAuditedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu_items")
public class MenuItem extends BaseAuditedEntity {

    @Column(name = "restaurant_id", nullable = false, updatable = false)
    private UUID restaurantId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private MenuCategory category;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_in_cents", nullable = false)
    private long priceInCents;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected MenuItem() {
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getPriceInCents() {
        return priceInCents;
    }

    public boolean isAvailable() {
        return available;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}

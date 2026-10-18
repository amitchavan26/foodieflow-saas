package com.fooddelivery.restaurant.domain;

import java.util.UUID;

import com.fooddelivery.common.persistence.BaseAuditedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu_categories")
public class MenuCategory extends BaseAuditedEntity {

    @Column(name = "restaurant_id", nullable = false, updatable = false)
    private UUID restaurantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", insertable = false, updatable = false)
    private Restaurant restaurant;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected MenuCategory() {
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}

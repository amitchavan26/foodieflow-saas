package com.fooddelivery.order.domain;

import java.util.UUID;

import com.fooddelivery.common.persistence.BaseAuditedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem extends BaseAuditedEntity {

    @Column(name = "restaurant_id", nullable = false, updatable = false)
    private UUID restaurantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "menu_item_id", nullable = false, updatable = false)
    private UUID menuItemId;

    @Column(name = "name_snapshot", nullable = false, length = 200)
    private String nameSnapshot;

    @Column(name = "unit_price_in_cents", nullable = false)
    private long unitPriceInCents;

    @Column(nullable = false)
    private int quantity;

    @Column(length = 300)
    private String notes;

    protected OrderItem() {
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public UUID getMenuItemId() {
        return menuItemId;
    }

    public String getNameSnapshot() {
        return nameSnapshot;
    }

    public long getUnitPriceInCents() {
        return unitPriceInCents;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getNotes() {
        return notes;
    }
}

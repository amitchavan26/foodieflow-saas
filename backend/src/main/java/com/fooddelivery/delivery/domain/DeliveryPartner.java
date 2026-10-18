package com.fooddelivery.delivery.domain;

import com.fooddelivery.common.persistence.BaseAuditedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "delivery_partners")
public class DeliveryPartner extends BaseAuditedEntity {

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeliveryPartnerStatus status = DeliveryPartnerStatus.OFFLINE;

    protected DeliveryPartner() {
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public DeliveryPartnerStatus getStatus() {
        return status;
    }
}

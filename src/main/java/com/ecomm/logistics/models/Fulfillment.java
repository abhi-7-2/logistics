package com.ecomm.logistics.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fulfillments")
@Getter
@Setter
public class Fulfillment {

    @Id
    private String id;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "external_fulfillment_id", nullable = false)
    private String externalFulfillmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FulfillmentStatus status;

    private String carrier;

    @Column(name = "service_level")
    private String serviceLevel;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum FulfillmentStatus {
        CREATED, SHIPPED, DELIVERED, CANCELLED, FAILED, UNKNOWN
    }
}

package com.ecomm.logistics.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    private String id;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(name = "website_id", nullable = false)
    private String websiteId;

    @Column(name = "external_order_id", nullable = false)
    private String externalOrderId;

    @Column(name = "external_order_number")
    private String externalOrderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "financial_status", nullable = false)
    private FinancialStatus financialStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", nullable = false)
    private FulfillmentOverallStatus fulfillmentStatus;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "order_total", nullable = false)
    private BigDecimal orderTotal;

    private String currency;

    @Column(name = "order_created_at")
    private LocalDateTime orderCreatedAt;

    @Column(name = "order_updated_at")
    private LocalDateTime orderUpdatedAt;

    @CreationTimestamp
    @Column(name = "ingested_at", updatable = false)
    private LocalDateTime ingestedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public enum OrderStatus {
        CREATED, CANCELLED, CLOSED
    }

    public enum FinancialStatus {
        UNKNOWN, PENDING, PAID, PARTIALLY_PAID, REFUNDED, PARTIALLY_REFUNDED, VOIDED
    }

    public enum FulfillmentOverallStatus {
        UNFULFILLED, PARTIAL, FULFILLED, CANCELLED, UNKNOWN
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}

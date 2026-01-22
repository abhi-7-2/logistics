package com.ecomm.logistics.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tracking")
@Getter
@Setter
public class Tracking {

    @Id
    private String id;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(name = "fulfillment_id", nullable = false)
    private String fulfillmentId;

    @Column(name = "tracking_number", nullable = false)
    private String trackingNumber;

    private String carrier;

    @Column(name = "tracking_url")
    private String trackingUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrackingStatus status;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary = false;

    @Column(name = "last_event_at")
    private LocalDateTime lastEventAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tracking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrackingEvent> events = new ArrayList<>();

    public enum TrackingStatus {
        LABEL_CREATED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, EXCEPTION, UNKNOWN
    }
}

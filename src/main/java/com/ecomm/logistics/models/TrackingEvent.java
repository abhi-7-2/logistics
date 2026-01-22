package com.ecomm.logistics.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_events")
@Getter
@Setter
public class TrackingEvent {

    @Id
    private String id;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_id", nullable = false)
    private Tracking tracking;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "event_code", nullable = false)
    private String eventCode;

    @Column(name = "event_description")
    private String eventDescription;

    @Column(name = "event_city")
    private String eventCity;

    @Column(name = "event_state")
    private String eventState;

    @Column(name = "event_country")
    private String eventCountry;

    @Column(name = "event_zip")
    private String eventZip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventSource source;

    @Column(name = "event_hash", nullable = false, unique = true)
    private String eventHash;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum EventSource {
        CARRIER, SHOPIFY, FENIX, OTHER
    }
}

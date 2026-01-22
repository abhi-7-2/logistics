package com.ecomm.logistics.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "websites")
@Getter
@Setter
public class Website {

    @Id
    private String id;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebsiteStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Platform {
        SHOPIFY, NETSUITE, CUSTOM, MAGENTO, OTHER
    }

    public enum WebsiteStatus {
        ACTIVE, INACTIVE
    }
}

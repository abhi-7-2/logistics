package com.ecomm.logistics.dtos;

import com.ecomm.logistics.models.Tracking.TrackingStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TrackingRequest {
    private String trackingNumber;
    private String carrier;
    private String trackingUrl;
    private TrackingStatus status;
    private boolean isPrimary;
    private LocalDateTime lastEventAt;
}

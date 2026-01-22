package com.ecomm.logistics.dtos;

import com.ecomm.logistics.models.Tracking.TrackingStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TrackingResponse {
    private String id;
    private String orgId;
    private String fulfillmentId;
    private String trackingNumber;
    private String carrier;
    private String trackingUrl;
    private TrackingStatus status;
    private boolean isPrimary;
    private LocalDateTime lastEventAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TrackingEventResponse> events;
}

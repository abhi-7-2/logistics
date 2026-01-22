package com.ecomm.logistics.dtos;

import com.ecomm.logistics.models.Fulfillment.FulfillmentStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class FulfillmentResponse {
    private String id;
    private String orgId;
    private String orderId;
    private String externalFulfillmentId;
    private FulfillmentStatus status;
    private String carrier;
    private String serviceLevel;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

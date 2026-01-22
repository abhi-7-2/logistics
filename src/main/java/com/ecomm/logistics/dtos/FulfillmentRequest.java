package com.ecomm.logistics.dtos;

import com.ecomm.logistics.models.Fulfillment.FulfillmentStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FulfillmentRequest {
    private String externalFulfillmentId;
    private FulfillmentStatus status;
    private String carrier;
    private String serviceLevel;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
}

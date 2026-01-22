package com.ecomm.logistics.dtos;

import com.ecomm.logistics.models.Order.FinancialStatus;
import com.ecomm.logistics.models.Order.FulfillmentOverallStatus;
import com.ecomm.logistics.models.Order.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private String id;
    private String orgId;
    private String websiteId;
    private String externalOrderId;
    private String externalOrderNumber;
    private OrderStatus status;
    private FinancialStatus financialStatus;
    private FulfillmentOverallStatus fulfillmentStatus;
    private String customerEmail;
    private BigDecimal orderTotal;
    private String currency;
    private LocalDateTime orderCreatedAt;
    private LocalDateTime orderUpdatedAt;
    private LocalDateTime ingestedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
}

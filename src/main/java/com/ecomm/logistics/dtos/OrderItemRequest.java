package com.ecomm.logistics.dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemRequest {
    private String externalLineItemId;
    private String sku;
    private String name;
    private Integer quantity;
    private BigDecimal price;
}

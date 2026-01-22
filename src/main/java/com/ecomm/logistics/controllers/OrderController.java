package com.ecomm.logistics.controllers;

import com.ecomm.logistics.dtos.OrderRequest;
import com.ecomm.logistics.dtos.OrderResponse;
import com.ecomm.logistics.dtos.PagedResponse;
import com.ecomm.logistics.models.Order.FinancialStatus;
import com.ecomm.logistics.models.Order.FulfillmentOverallStatus;
import com.ecomm.logistics.models.Order.OrderStatus;
import com.ecomm.logistics.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrderById(@PathVariable String orderId) {
        return orderService.getOrderById(orderId);
    }

    @GetMapping
    public PagedResponse<OrderResponse> listOrders(
            @RequestParam String orgId,
            @RequestParam(required = false) String websiteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) FinancialStatus financialStatus,
            @RequestParam(required = false) FulfillmentOverallStatus fulfillmentStatus) {
        return orderService.listOrders(orgId, websiteId, from, to, page, size, sort, status, financialStatus, fulfillmentStatus);
    }

    @GetMapping("/search")
    public PagedResponse<OrderResponse> searchOrders(
            @RequestParam String orgId,
            @RequestParam(required = false) String websiteId,
            @RequestParam(required = false) String externalOrderId,
            @RequestParam(required = false) String externalOrderNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return orderService.searchOrdersByExternal(orgId, websiteId, externalOrderId, externalOrderNumber, page, size);
    }

    @PutMapping("/{orderId}")
    public OrderResponse updateOrder(
            @PathVariable String orderId,
            @Valid @RequestBody OrderRequest request) {
        return orderService.updateOrder(orderId, request);
    }

    @PatchMapping("/{orderId}")
    public OrderResponse patchOrder(
            @PathVariable String orderId,
            @RequestBody OrderRequest request) {
        return orderService.patchOrder(orderId, request);
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable String orderId) {
        orderService.deleteOrder(orderId);
    }
}

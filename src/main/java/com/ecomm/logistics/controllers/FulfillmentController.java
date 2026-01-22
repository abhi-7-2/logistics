package com.ecomm.logistics.controllers;

import com.ecomm.logistics.dtos.FulfillmentRequest;
import com.ecomm.logistics.dtos.FulfillmentResponse;
import com.ecomm.logistics.dtos.PagedResponse;
import com.ecomm.logistics.models.Fulfillment.FulfillmentStatus;
import com.ecomm.logistics.services.FulfillmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/orders/{orderId}/fulfillments")
public class FulfillmentController {

    private final FulfillmentService fulfillmentService;

    public FulfillmentController(FulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FulfillmentResponse createFulfillment(
            @PathVariable String orderId,
            @Valid @RequestBody FulfillmentRequest request) {
        return fulfillmentService.createFulfillment(orderId, request);
    }

    @GetMapping
    public PagedResponse<FulfillmentResponse> listFulfillments(
            @PathVariable String orderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort,
            @RequestParam(required = false) FulfillmentStatus status,
            @RequestParam(required = false) String carrier) {
        return fulfillmentService.listFulfillments(orderId, from, to, page, size, sort, status, carrier);
    }

    @GetMapping("/search")
    public PagedResponse<FulfillmentResponse> searchFulfillments(
            @PathVariable String orderId,
            @RequestParam String externalFulfillmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return fulfillmentService.searchFulfillmentsByExternal(orderId, externalFulfillmentId, page, size);
    }

    @GetMapping("/{fulfillmentId}")
    public FulfillmentResponse getFulfillment(
            @PathVariable String orderId,
            @PathVariable String fulfillmentId) {
        return fulfillmentService.getFulfillmentById(orderId, fulfillmentId);
    }

    @PutMapping("/{fulfillmentId}")
    public FulfillmentResponse updateFulfillment(
            @PathVariable String orderId,
            @PathVariable String fulfillmentId,
            @Valid @RequestBody FulfillmentRequest request) {
        return fulfillmentService.updateFulfillment(orderId, fulfillmentId, request);
    }

    @PatchMapping("/{fulfillmentId}")
    public FulfillmentResponse patchFulfillment(
            @PathVariable String orderId,
            @PathVariable String fulfillmentId,
            @RequestBody FulfillmentRequest request) {
        return fulfillmentService.patchFulfillment(orderId, fulfillmentId, request);
    }

    @DeleteMapping("/{fulfillmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFulfillment(
            @PathVariable String orderId,
            @PathVariable String fulfillmentId) {
        fulfillmentService.deleteFulfillment(orderId, fulfillmentId);
    }
}

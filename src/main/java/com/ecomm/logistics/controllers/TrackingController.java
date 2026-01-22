package com.ecomm.logistics.controllers;

import com.ecomm.logistics.dtos.PagedResponse;
import com.ecomm.logistics.dtos.TrackingEventRequest;
import com.ecomm.logistics.dtos.TrackingEventResponse;
import com.ecomm.logistics.dtos.TrackingRequest;
import com.ecomm.logistics.dtos.TrackingResponse;
import com.ecomm.logistics.models.Tracking.TrackingStatus;
import com.ecomm.logistics.services.TrackingEventService;
import com.ecomm.logistics.services.TrackingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/fulfillments/{fulfillmentId}/tracking")
public class TrackingController {

    private final TrackingService trackingService;
    private final TrackingEventService eventService;

    public TrackingController(TrackingService trackingService, TrackingEventService eventService) {
        this.trackingService = trackingService;
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrackingResponse createTracking(
            @PathVariable String fulfillmentId,
            @Valid @RequestBody TrackingRequest request) {
        return trackingService.createTracking(fulfillmentId, request);
    }

    @GetMapping
    public PagedResponse<TrackingResponse> listTracking(
            @PathVariable String fulfillmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort,
            @RequestParam(required = false) TrackingStatus status,
            @RequestParam(required = false) String carrier,
            @RequestParam(required = false) String trackingNumber) {
        return trackingService.listTracking(fulfillmentId, from, to, page, size, sort, status, carrier, trackingNumber);
    }

    @GetMapping("/search")
    public PagedResponse<TrackingResponse> searchTracking(
            @PathVariable String fulfillmentId,
            @RequestParam String trackingNumber,
            @RequestParam(required = false) String carrier,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return trackingService.searchTrackingByNumber(fulfillmentId, trackingNumber, carrier, page, size);
    }

    @GetMapping("/{trackingId}")
    public TrackingResponse getTracking(
            @PathVariable String fulfillmentId,
            @PathVariable String trackingId) {
        return trackingService.getTrackingById(fulfillmentId, trackingId);
    }

    @PutMapping("/{trackingId}")
    public TrackingResponse updateTracking(
            @PathVariable String fulfillmentId,
            @PathVariable String trackingId,
            @Valid @RequestBody TrackingRequest request) {
        return trackingService.updateTracking(fulfillmentId, trackingId, request);
    }

    @PatchMapping("/{trackingId}")
    public TrackingResponse patchTracking(
            @PathVariable String fulfillmentId,
            @PathVariable String trackingId,
            @RequestBody TrackingRequest request) {
        return trackingService.patchTracking(fulfillmentId, trackingId, request);
    }

    @DeleteMapping("/{trackingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTracking(
            @PathVariable String fulfillmentId,
            @PathVariable String trackingId) {
        trackingService.deleteTracking(fulfillmentId, trackingId);
    }

    @PostMapping("/{trackingId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public TrackingEventResponse createTrackingEvent(
            @PathVariable String fulfillmentId,
            @PathVariable String trackingId,
            @Valid @RequestBody TrackingEventRequest request) {
        // Get tracking to extract orgId
        TrackingResponse tracking = trackingService.getTrackingById(fulfillmentId, trackingId);
        return eventService.ingestEvent(tracking.getOrgId(), trackingId, request);
    }
}

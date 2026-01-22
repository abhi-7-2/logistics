package com.ecomm.logistics.services;

import com.ecomm.logistics.dtos.PagedResponse;
import com.ecomm.logistics.dtos.TrackingRequest;
import com.ecomm.logistics.dtos.TrackingResponse;
import com.ecomm.logistics.exceptions.ResourceNotFoundException;
import com.ecomm.logistics.models.Tracking;
import com.ecomm.logistics.models.Tracking.TrackingStatus;
import com.ecomm.logistics.repositories.FulfillmentRepository;
import com.ecomm.logistics.repositories.TrackingRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.security.SecureRandom;
import java.util.stream.Collectors;

@Service
public class TrackingService {

    private final TrackingRepository trackingRepository;
    private final FulfillmentRepository fulfillmentRepository;
    private final ModelMapper modelMapper;
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();

    public TrackingService(TrackingRepository trackingRepository,
            FulfillmentRepository fulfillmentRepository,
            ModelMapper modelMapper) {
        this.trackingRepository = trackingRepository;
        this.fulfillmentRepository = fulfillmentRepository;
        this.modelMapper = modelMapper;
    }

    private String generateAlphanumericId(int length, String prefix) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return prefix + sb.toString().substring(prefix.length());
    }

    @Transactional
    public TrackingResponse createTracking(String fulfillmentId, TrackingRequest request) {
        com.ecomm.logistics.models.Fulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Fulfillment not found with id: " + fulfillmentId));

        // Upsert by tracking number within the fulfillment
        return trackingRepository.findByFulfillmentIdAndTrackingNumber(fulfillmentId, request.getTrackingNumber())
                .map(existing -> {
                    modelMapper.map(request, existing);
                    return mapToResponse(trackingRepository.save(existing));
                })
                .orElseGet(() -> {
                    Tracking tracking = modelMapper.map(request, Tracking.class);
                    tracking.setId(generateAlphanumericId(12, "TRK"));
                    tracking.setOrgId(fulfillment.getOrgId());
                    tracking.setFulfillmentId(fulfillmentId);
                    if (tracking.getStatus() == null)
                        tracking.setStatus(TrackingStatus.UNKNOWN);
                    return mapToResponse(trackingRepository.save(tracking));
                });
    }

    public TrackingResponse getTrackingById(String fulfillmentId, String trackingId) {
        Tracking tracking = trackingRepository.findById(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking not found with id: " + trackingId));
        
        if (!tracking.getFulfillmentId().equals(fulfillmentId)) {
            throw new ResourceNotFoundException("Tracking not found in this fulfillment");
        }
        
        return mapToResponse(tracking);
    }

    public PagedResponse<TrackingResponse> listTracking(String fulfillmentId,
            java.time.LocalDateTime from, java.time.LocalDateTime to,
            int page, int size, String sort,
            TrackingStatus status, String carrier, String trackingNumber) {
        String[] sortParts = sort.split(",");
        String property = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));
        Specification<Tracking> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("fulfillmentId"), fulfillmentId));
            
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (carrier != null) {
                predicates.add(cb.like(cb.lower(root.get("carrier")), "%" + carrier.toLowerCase() + "%"));
            }
            if (trackingNumber != null) {
                predicates.add(cb.like(cb.lower(root.get("trackingNumber")), "%" + trackingNumber.toLowerCase() + "%"));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Tracking> trackingPage = trackingRepository.findAll(spec, pageable);

        return PagedResponse.<TrackingResponse>builder()
                .data(trackingPage.getContent().stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()))
                .page(trackingPage.getNumber())
                .size(trackingPage.getSize())
                .totalElements(trackingPage.getTotalElements())
                .totalPages(trackingPage.getTotalPages())
                .hasNext(trackingPage.hasNext())
                .build();
    }

    public PagedResponse<TrackingResponse> searchTrackingByNumber(String fulfillmentId, String trackingNumber,
            String carrier, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Tracking> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("fulfillmentId"), fulfillmentId));
            predicates.add(cb.equal(root.get("trackingNumber"), trackingNumber));
            if (carrier != null) {
                predicates.add(cb.like(cb.lower(root.get("carrier")), "%" + carrier.toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Tracking> trackingPage = trackingRepository.findAll(spec, pageable);

        return PagedResponse.<TrackingResponse>builder()
                .data(trackingPage.getContent().stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()))
                .page(trackingPage.getNumber())
                .size(trackingPage.getSize())
                .totalElements(trackingPage.getTotalElements())
                .totalPages(trackingPage.getTotalPages())
                .hasNext(trackingPage.hasNext())
                .build();
    }

    @Transactional
    public TrackingResponse updateTracking(String fulfillmentId, String trackingId, TrackingRequest request) {
        Tracking tracking = trackingRepository.findById(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking not found with id: " + trackingId));

        if (!tracking.getFulfillmentId().equals(fulfillmentId)) {
            throw new ResourceNotFoundException("Tracking not found in this fulfillment");
        }

        modelMapper.map(request, tracking);
        return mapToResponse(trackingRepository.save(tracking));
    }

    @Transactional
    public TrackingResponse patchTracking(String fulfillmentId, String trackingId, TrackingRequest request) {
        Tracking tracking = trackingRepository.findById(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking not found with id: " + trackingId));

        if (!tracking.getFulfillmentId().equals(fulfillmentId)) {
            throw new ResourceNotFoundException("Tracking not found in this fulfillment");
        }

        // Only update non-null fields
        if (request.getCarrier() != null) tracking.setCarrier(request.getCarrier());
        if (request.getTrackingUrl() != null) tracking.setTrackingUrl(request.getTrackingUrl());
        if (request.getStatus() != null) tracking.setStatus(request.getStatus());
        // Note: boolean primitive - always update (Lombok generates setIsPrimary for boolean isPrimary field)
        tracking.setPrimary(request.isPrimary());
        
        if (request.getLastEventAt() != null) tracking.setLastEventAt(request.getLastEventAt());
        
        return mapToResponse(trackingRepository.save(tracking));
    }

    @Transactional
    public void deleteTracking(String fulfillmentId, String trackingId) {
        Tracking tracking = trackingRepository.findById(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking not found with id: " + trackingId));
        
        if (!tracking.getFulfillmentId().equals(fulfillmentId)) {
            throw new ResourceNotFoundException("Tracking not found in this fulfillment");
        }
        
        trackingRepository.delete(tracking);
    }

    private TrackingResponse mapToResponse(Tracking tracking) {
        TrackingResponse res = modelMapper.map(tracking, TrackingResponse.class);
        // Events are mapped if present in child collection
        return res;
    }
}

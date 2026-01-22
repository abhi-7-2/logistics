package com.ecomm.logistics.services;

import com.ecomm.logistics.dtos.TrackingEventRequest;
import com.ecomm.logistics.dtos.TrackingEventResponse;
import com.ecomm.logistics.exceptions.ResourceNotFoundException;
import com.ecomm.logistics.models.Tracking;
import com.ecomm.logistics.models.Tracking.TrackingStatus;
import com.ecomm.logistics.models.TrackingEvent;
import com.ecomm.logistics.repositories.TrackingEventRepository;
import com.ecomm.logistics.repositories.TrackingRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

@Service
public class TrackingEventService {

    private final TrackingEventRepository eventRepository;
    private final TrackingRepository trackingRepository;
    private final ModelMapper modelMapper;
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();

    public TrackingEventService(TrackingEventRepository eventRepository,
            TrackingRepository trackingRepository,
            ModelMapper modelMapper) {
        this.eventRepository = eventRepository;
        this.trackingRepository = trackingRepository;
        this.modelMapper = modelMapper;
    }

    private String generateAlphanumericId(int length, String prefix) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return prefix + sb.toString().substring(prefix.length());
    }

    private String calculateHash(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @Transactional
    public TrackingEventResponse ingestEvent(String orgId, String trackingId, TrackingEventRequest request) {
        Tracking tracking = trackingRepository.findByOrgIdAndId(orgId, trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking not found with id: " + trackingId));

        // Generate hash for idempotency: orgId + trackingId + eventTime + eventCode
        String base = orgId + trackingId + request.getEventTime().toString() + request.getEventCode();
        String hash = calculateHash(base);

        return eventRepository.findByOrgIdAndEventHash(orgId, hash)
                .map(existing -> modelMapper.map(existing, TrackingEventResponse.class))
                .orElseGet(() -> {
                    TrackingEvent event = modelMapper.map(request, TrackingEvent.class);
                    event.setId(generateAlphanumericId(12, "EVT"));
                    event.setOrgId(orgId);
                    event.setTracking(tracking);
                    event.setEventHash(hash);

                    TrackingEvent saved = eventRepository.save(event);
                    updateTrackingRollup(tracking, event);
                    return modelMapper.map(saved, TrackingEventResponse.class);
                });
    }

    private void updateTrackingRollup(Tracking tracking, TrackingEvent event) {
        // Update lastEventAt if new event is later
        if (tracking.getLastEventAt() == null || event.getEventTime().isAfter(tracking.getLastEventAt())) {
            tracking.setLastEventAt(event.getEventTime());

            // Heuristic status update based on event code (simulated)
            String code = event.getEventCode().toUpperCase();
            if (code.contains("DELIVERED")) {
                tracking.setStatus(TrackingStatus.DELIVERED);
            } else if (code.contains("OUT") || code.contains("DELIVERY")) {
                tracking.setStatus(TrackingStatus.OUT_FOR_DELIVERY);
            } else if (code.contains("TRANSIT") || code.contains("SHIPPED")) {
                tracking.setStatus(TrackingStatus.IN_TRANSIT);
            } else if (code.contains("EXCEPTION") || code.contains("FAIL")) {
                tracking.setStatus(TrackingStatus.EXCEPTION);
            } else if (code.contains("LABEL") || code.contains("PICKUP")) {
                tracking.setStatus(TrackingStatus.LABEL_CREATED);
            }

            trackingRepository.save(tracking);
        }
    }
}

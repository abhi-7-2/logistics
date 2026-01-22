package com.ecomm.logistics.repositories;

import com.ecomm.logistics.models.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, String> {
    List<TrackingEvent> findByTrackingIdOrderByEventTimeDesc(String trackingId);

    Optional<TrackingEvent> findByOrgIdAndEventHash(String orgId, String eventHash);
}

package com.ecomm.logistics.repositories;

import com.ecomm.logistics.models.Tracking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrackingRepository extends JpaRepository<Tracking, String>, JpaSpecificationExecutor<Tracking> {
    Page<Tracking> findByOrgIdAndFulfillmentId(String orgId, String fulfillmentId, Pageable pageable);

    Optional<Tracking> findByOrgIdAndId(String orgId, String id);

    Optional<Tracking> findByOrgIdAndTrackingNumber(String orgId, String trackingNumber);
    
    Optional<Tracking> findByFulfillmentIdAndTrackingNumber(String fulfillmentId, String trackingNumber);
}

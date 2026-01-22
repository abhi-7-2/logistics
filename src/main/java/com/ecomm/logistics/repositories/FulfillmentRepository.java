package com.ecomm.logistics.repositories;

import com.ecomm.logistics.models.Fulfillment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FulfillmentRepository extends JpaRepository<Fulfillment, String>, JpaSpecificationExecutor<Fulfillment> {

    Page<Fulfillment> findByOrgIdAndOrderId(String orgId, String orderId, Pageable pageable);

    Optional<Fulfillment> findByOrgIdAndOrderIdAndExternalFulfillmentId(String orgId, String orderId,
            String externalFulfillmentId);

    Optional<Fulfillment> findByOrgIdAndId(String orgId, String id);
}

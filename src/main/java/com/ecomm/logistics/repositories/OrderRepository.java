package com.ecomm.logistics.repositories;

import com.ecomm.logistics.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {

        Page<Order> findByOrgId(String orgId, Pageable pageable);

        Page<Order> findByOrgIdAndWebsiteId(String orgId, String websiteId, Pageable pageable);

        Page<Order> findByOrgIdAndIngestedAtBetween(String orgId, LocalDateTime from, LocalDateTime to,
                        Pageable pageable);

        Optional<Order> findByOrgIdAndWebsiteIdAndExternalOrderId(String orgId, String websiteId,
                        String externalOrderId);

        Page<Order> findByOrgIdAndExternalOrderNumberContainingIgnoreCase(String orgId, String externalOrderNumber,
                        Pageable pageable);

        Page<Order> findByOrgIdAndExternalOrderIdContainingIgnoreCase(String orgId, String externalOrderId,
                        Pageable pageable);

        Page<Order> findByOrgIdAndCustomerEmailContainingIgnoreCase(String orgId, String customerEmail,
                        Pageable pageable);
}

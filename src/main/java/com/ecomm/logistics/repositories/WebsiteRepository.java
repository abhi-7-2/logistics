package com.ecomm.logistics.repositories;

import com.ecomm.logistics.models.Website;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebsiteRepository extends JpaRepository<Website, String>, JpaSpecificationExecutor<Website> {
    Page<Website> findByOrgId(String orgId, Pageable pageable);

    Page<Website> findByOrgIdAndStatus(String orgId, Website.WebsiteStatus status, Pageable pageable);

    Page<Website> findByOrgIdAndPlatform(String orgId, Website.Platform platform, Pageable pageable);

    Page<Website> findByOrgIdAndCodeContainingIgnoreCase(String orgId, String code, Pageable pageable);

    Page<Website> findByOrgIdAndDomainContainingIgnoreCase(String orgId, String domain, Pageable pageable);

    Optional<Website> findByOrgIdAndId(String orgId, String id);

    boolean existsByOrgIdAndId(String orgId, String id);
}

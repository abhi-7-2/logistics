package com.ecomm.logistics.repositories;

import com.ecomm.logistics.models.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, String>, JpaSpecificationExecutor<Organization> {
    java.util.Optional<Organization> findByExternalId(String externalId);

    Page<Organization> findByNameContainingIgnoreCaseAndStatus(String name, Organization.OrgStatus status,
            org.springframework.data.domain.Pageable pageable);

    Page<Organization> findByExternalIdContaining(String externalId, org.springframework.data.domain.Pageable pageable);

    Page<Organization> findByStatus(Organization.OrgStatus status, org.springframework.data.domain.Pageable pageable);

    Page<Organization> findByNameContainingIgnoreCase(String name, org.springframework.data.domain.Pageable pageable);
}

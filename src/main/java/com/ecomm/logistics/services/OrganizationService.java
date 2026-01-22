package com.ecomm.logistics.services;

import com.ecomm.logistics.dtos.OrganizationRequest;
import com.ecomm.logistics.dtos.OrganizationResponse;
import com.ecomm.logistics.dtos.PagedResponse;
import com.ecomm.logistics.exceptions.ResourceNotFoundException;
import com.ecomm.logistics.models.Organization;
import com.ecomm.logistics.models.Organization.OrgStatus;
import com.ecomm.logistics.repositories.OrganizationRepository;
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
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final ModelMapper modelMapper;
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();

    public OrganizationService(OrganizationRepository organizationRepository, ModelMapper modelMapper) {
        this.organizationRepository = organizationRepository;
        this.modelMapper = modelMapper;
    }

    private String generateAlphanumericId(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    @Transactional
    public OrganizationResponse createOrganization(OrganizationRequest request) {
        Organization org = modelMapper.map(request, Organization.class);
        org.setId(generateAlphanumericId(12));
        if (org.getStatus() == null) {
            org.setStatus(OrgStatus.ACTIVE);
        }
        Organization saved = organizationRepository.save(org);
        return modelMapper.map(saved, OrganizationResponse.class);
    }

    public OrganizationResponse getOrganization(String id) {
        return organizationRepository.findById(id)
                .map(org -> modelMapper.map(org, OrganizationResponse.class))
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));
    }

    public PagedResponse<OrganizationResponse> searchByExternalId(String externalId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Organization> spec = (root, query, cb) -> {
            return cb.like(cb.lower(root.get("externalId")), "%" + externalId.toLowerCase() + "%");
        };
        Page<Organization> orgPage = organizationRepository.findAll(spec, pageable);

        return PagedResponse.<OrganizationResponse>builder()
                .data(orgPage.getContent().stream()
                        .map(org -> modelMapper.map(org, OrganizationResponse.class))
                        .collect(Collectors.toList()))
                .page(orgPage.getNumber())
                .size(orgPage.getSize())
                .totalElements(orgPage.getTotalElements())
                .totalPages(orgPage.getTotalPages())
                .hasNext(orgPage.hasNext())
                .build();
    }

    public PagedResponse<OrganizationResponse> listOrganizations(String name, OrgStatus status,
            java.time.LocalDateTime from, java.time.LocalDateTime to,
            int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String property = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));
        Specification<Organization> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<Predicate>();
            
            if (name != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Organization> orgPage = organizationRepository.findAll(spec, pageable);

        return PagedResponse.<OrganizationResponse>builder()
                .data(orgPage.getContent().stream()
                        .map(org -> modelMapper.map(org, OrganizationResponse.class))
                        .collect(Collectors.toList()))
                .page(orgPage.getNumber())
                .size(orgPage.getSize())
                .totalElements(orgPage.getTotalElements())
                .totalPages(orgPage.getTotalPages())
                .hasNext(orgPage.hasNext())
                .build();
    }

    @Transactional
    public OrganizationResponse updateOrganization(String id, OrganizationRequest request) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));

        modelMapper.map(request, org);
        Organization saved = organizationRepository.save(org);
        return modelMapper.map(saved, OrganizationResponse.class);
    }

    @Transactional
    public OrganizationResponse patchOrganization(String id, OrganizationRequest request) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));

        modelMapper.map(request, org);
        Organization saved = organizationRepository.save(org);
        return modelMapper.map(saved, OrganizationResponse.class);
    }

    @Transactional
    public void deleteOrganization(String id) {
        if (!organizationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Organization not found with id: " + id);
        }
        organizationRepository.deleteById(id);
    }
}

package com.ecomm.logistics.services;

import com.ecomm.logistics.dtos.PagedResponse;
import com.ecomm.logistics.dtos.WebsiteRequest;
import com.ecomm.logistics.dtos.WebsiteResponse;
import com.ecomm.logistics.exceptions.ResourceNotFoundException;
import com.ecomm.logistics.models.Website;
import com.ecomm.logistics.models.Website.Platform;
import com.ecomm.logistics.models.Website.WebsiteStatus;
import com.ecomm.logistics.repositories.OrganizationRepository;
import com.ecomm.logistics.repositories.WebsiteRepository;
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
public class WebsiteService {

    private final WebsiteRepository websiteRepository;
    private final OrganizationRepository organizationRepository;
    private final ModelMapper modelMapper;
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();

    public WebsiteService(WebsiteRepository websiteRepository, OrganizationRepository organizationRepository,
            ModelMapper modelMapper) {
        this.websiteRepository = websiteRepository;
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

    private void validateOrganization(String orgId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization not found with id: " + orgId);
        }
    }

    @Transactional
    public WebsiteResponse createWebsite(String orgId, WebsiteRequest request) {
        validateOrganization(orgId);

        Website website = modelMapper.map(request, Website.class);
        website.setId(generateAlphanumericId(12));
        website.setOrgId(orgId);
        if (website.getStatus() == null) {
            website.setStatus(WebsiteStatus.ACTIVE);
        }

        Website saved = websiteRepository.save(website);
        return modelMapper.map(saved, WebsiteResponse.class);
    }

    public WebsiteResponse getWebsite(String orgId, String websiteId) {
        return websiteRepository.findByOrgIdAndId(orgId, websiteId)
                .map(site -> modelMapper.map(site, WebsiteResponse.class))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Website not found with id: " + websiteId + " for organization: " + orgId));
    }

    public PagedResponse<WebsiteResponse> listWebsites(String orgId,
            java.time.LocalDateTime from, java.time.LocalDateTime to,
            int page, int size, String sort,
            WebsiteStatus status, Platform platform,
            String code, String domain) {
        validateOrganization(orgId);

        String[] sortParts = sort.split(",");
        String property = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));
        Specification<Website> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("orgId"), orgId));
            
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (platform != null) {
                predicates.add(cb.equal(root.get("platform"), platform));
            }
            if (code != null) {
                predicates.add(cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
            }
            if (domain != null) {
                predicates.add(cb.like(cb.lower(root.get("domain")), "%" + domain.toLowerCase() + "%"));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Website> websitePage = websiteRepository.findAll(spec, pageable);

        return PagedResponse.<WebsiteResponse>builder()
                .data(websitePage.getContent().stream()
                        .map(website -> modelMapper.map(website, WebsiteResponse.class))
                        .collect(Collectors.toList()))
                .page(websitePage.getNumber())
                .size(websitePage.getSize())
                .totalElements(websitePage.getTotalElements())
                .totalPages(websitePage.getTotalPages())
                .hasNext(websitePage.hasNext())
                .build();
    }

    public PagedResponse<WebsiteResponse> searchWebsites(String orgId, String websiteId, String code, String domain,
            int page, int size) {
        validateOrganization(orgId);
        
        Pageable pageable = PageRequest.of(page, size);
        Specification<Website> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("orgId"), orgId));
            
            if (websiteId != null) {
                predicates.add(cb.equal(root.get("id"), websiteId));
            }
            if (code != null) {
                predicates.add(cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
            }
            if (domain != null) {
                predicates.add(cb.like(cb.lower(root.get("domain")), "%" + domain.toLowerCase() + "%"));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Website> websitePage = websiteRepository.findAll(spec, pageable);

        return PagedResponse.<WebsiteResponse>builder()
                .data(websitePage.getContent().stream()
                        .map(website -> modelMapper.map(website, WebsiteResponse.class))
                        .collect(Collectors.toList()))
                .page(websitePage.getNumber())
                .size(websitePage.getSize())
                .totalElements(websitePage.getTotalElements())
                .totalPages(websitePage.getTotalPages())
                .hasNext(websitePage.hasNext())
                .build();
    }

    @Transactional
    public WebsiteResponse updateWebsite(String orgId, String websiteId, WebsiteRequest request) {
        Website website = websiteRepository.findByOrgIdAndId(orgId, websiteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Website not found with id: " + websiteId + " for organization: " + orgId));

        modelMapper.map(request, website);
        Website saved = websiteRepository.save(website);
        return modelMapper.map(saved, WebsiteResponse.class);
    }

    @Transactional
    public WebsiteResponse patchWebsite(String orgId, String websiteId, WebsiteRequest request) {
        Website website = websiteRepository.findByOrgIdAndId(orgId, websiteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Website not found with id: " + websiteId + " for organization: " + orgId));

        modelMapper.map(request, website);
        Website saved = websiteRepository.save(website);
        return modelMapper.map(saved, WebsiteResponse.class);
    }

    @Transactional
    public void deleteWebsite(String orgId, String websiteId) {
        Website website = websiteRepository.findByOrgIdAndId(orgId, websiteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Website not found with id: " + websiteId + " for organization: " + orgId));
        websiteRepository.delete(website);
    }
}

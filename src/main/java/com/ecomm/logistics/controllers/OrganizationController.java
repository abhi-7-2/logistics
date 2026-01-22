package com.ecomm.logistics.controllers;

import com.ecomm.logistics.dtos.OrganizationRequest;
import com.ecomm.logistics.dtos.OrganizationResponse;
import com.ecomm.logistics.dtos.PagedResponse;
import com.ecomm.logistics.services.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationResponse createOrganization(@Valid @RequestBody OrganizationRequest request) {
        return organizationService.createOrganization(request);
    }

    @GetMapping("/{id}")
    public OrganizationResponse getOrganization(@PathVariable String id) {
        return organizationService.getOrganization(id);
    }

    @GetMapping
    public PagedResponse<OrganizationResponse> listOrganizations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort,
            @RequestParam(required = false) com.ecomm.logistics.models.Organization.OrgStatus status,
            @RequestParam(required = false) String name) {
        return organizationService.listOrganizations(name, status, from, to, page, size, sort);
    }

    @GetMapping("/search")
    public PagedResponse<OrganizationResponse> searchOrganizationsByRef(
            @RequestParam String externalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return organizationService.searchByExternalId(externalId, page, size);
    }

    @PutMapping("/{id}")
    public OrganizationResponse updateOrganization(
            @PathVariable String id,
            @Valid @RequestBody OrganizationRequest request) {
        return organizationService.updateOrganization(id, request);
    }

    @PatchMapping("/{id}")
    public OrganizationResponse patchOrganization(
            @PathVariable String id,
            @RequestBody OrganizationRequest request) {
        return organizationService.patchOrganization(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrganization(@PathVariable String id) {
        organizationService.deleteOrganization(id);
    }
}

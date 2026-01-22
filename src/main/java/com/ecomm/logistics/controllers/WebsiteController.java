package com.ecomm.logistics.controllers;

import com.ecomm.logistics.dtos.PagedResponse;
import com.ecomm.logistics.dtos.WebsiteRequest;
import com.ecomm.logistics.dtos.WebsiteResponse;
import com.ecomm.logistics.models.Website.Platform;
import com.ecomm.logistics.models.Website.WebsiteStatus;
import com.ecomm.logistics.services.WebsiteService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/organizations/{orgId}/websites")
public class WebsiteController {

    private final WebsiteService websiteService;

    public WebsiteController(WebsiteService websiteService) {
        this.websiteService = websiteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WebsiteResponse createWebsite(
            @PathVariable String orgId,
            @Valid @RequestBody WebsiteRequest request) {
        return websiteService.createWebsite(orgId, request);
    }

    @GetMapping("/{websiteId}")
    public WebsiteResponse getWebsite(
            @PathVariable String orgId,
            @PathVariable String websiteId) {
        return websiteService.getWebsite(orgId, websiteId);
    }

    @GetMapping
    public PagedResponse<WebsiteResponse> listWebsites(
            @PathVariable String orgId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort,
            @RequestParam(required = false) WebsiteStatus status,
            @RequestParam(required = false) Platform platform,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String domain) {
        return websiteService.listWebsites(orgId, from, to, page, size, sort, status, platform, code, domain);
    }

    @GetMapping("/search")
    public PagedResponse<WebsiteResponse> searchWebsites(
            @PathVariable String orgId,
            @RequestParam(required = false) String websiteId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String domain,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return websiteService.searchWebsites(orgId, websiteId, code, domain, page, size);
    }

    @PutMapping("/{websiteId}")
    public WebsiteResponse updateWebsite(
            @PathVariable String orgId,
            @PathVariable String websiteId,
            @Valid @RequestBody WebsiteRequest request) {
        return websiteService.updateWebsite(orgId, websiteId, request);
    }

    @PatchMapping("/{websiteId}")
    public WebsiteResponse patchWebsite(
            @PathVariable String orgId,
            @PathVariable String websiteId,
            @RequestBody WebsiteRequest request) {
        return websiteService.patchWebsite(orgId, websiteId, request);
    }

    @DeleteMapping("/{websiteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWebsite(
            @PathVariable String orgId,
            @PathVariable String websiteId) {
        websiteService.deleteWebsite(orgId, websiteId);
    }
}

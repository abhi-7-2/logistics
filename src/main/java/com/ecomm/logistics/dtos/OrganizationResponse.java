package com.ecomm.logistics.dtos;

import com.ecomm.logistics.models.Organization.OrgStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OrganizationResponse {
    private String id;
    private String externalId;
    private String name;
    private OrgStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

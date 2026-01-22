package com.ecomm.logistics.dtos;

import com.ecomm.logistics.models.Organization.OrgStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrganizationRequest {
    @Size(min = 2, message = "Name must be at least 2 characters")
    private String name;
    private String externalId;
    private OrgStatus status;
}

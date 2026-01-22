package com.ecomm.logistics.dtos;

import com.ecomm.logistics.models.Website.Platform;
import com.ecomm.logistics.models.Website.WebsiteStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WebsiteRequest {
    @Size(min = 2, message = "Code must be at least 2 characters")
    private String code;
    @Size(min = 2, message = "Name must be at least 2 characters")
    private String name;
    private Platform platform;
    private String domain;
    private WebsiteStatus status;
}

package com.ecomm.logistics.dtos;

import com.ecomm.logistics.models.Website.Platform;
import com.ecomm.logistics.models.Website.WebsiteStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class WebsiteResponse {
    private String id;
    private String orgId;
    private String code;
    private String name;
    private Platform platform;
    private String domain;
    private WebsiteStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

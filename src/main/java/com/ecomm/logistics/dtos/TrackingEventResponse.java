package com.ecomm.logistics.dtos;

import com.ecomm.logistics.models.TrackingEvent.EventSource;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class TrackingEventResponse {
    private String id;
    private String orgId;
    private String trackingId;
    private LocalDateTime eventTime;
    private String eventCode;
    private String eventDescription;
    private String eventCity;
    private String eventState;
    private String eventCountry;
    private String eventZip;
    private EventSource source;
    private String eventHash;
    private LocalDateTime createdAt;
}

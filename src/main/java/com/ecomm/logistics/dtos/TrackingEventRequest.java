package com.ecomm.logistics.dtos;

import com.ecomm.logistics.models.TrackingEvent.EventSource;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TrackingEventRequest {
    private LocalDateTime eventTime;
    private String eventCode;
    private String eventDescription;
    private String eventCity;
    private String eventState;
    private String eventCountry;
    private String eventZip;
    private EventSource source;
}

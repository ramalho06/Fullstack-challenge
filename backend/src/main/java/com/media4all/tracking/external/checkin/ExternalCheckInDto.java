package com.media4all.tracking.external.checkin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.media4all.tracking.checkin.CheckInSource;
import com.media4all.tracking.checkin.CheckInType;

import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalCheckInDto(
        String id,
        String agentId,
        CheckInType type,
        CheckInSource source,
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        BigDecimal accuracy,
        BigDecimal speed,
        String notes,
        BigDecimal distanceFromPrevious,
        String externalEventId,
        Instant occurredAt,
        Instant syncedAt
) {
}

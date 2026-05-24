package com.media4all.tracking.checkin.dto;

import com.media4all.tracking.checkin.CheckInSource;
import com.media4all.tracking.checkin.CheckInType;

import java.math.BigDecimal;
import java.time.Instant;

public record CheckInResponse(
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

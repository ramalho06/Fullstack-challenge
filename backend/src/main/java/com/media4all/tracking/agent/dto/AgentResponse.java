package com.media4all.tracking.agent.dto;

import com.media4all.tracking.agent.AgentRole;
import com.media4all.tracking.agent.AgentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record AgentResponse(
        String id,
        String externalId,
        String name,
        AgentRole role,
        String team,
        String phone,
        String email,
        Boolean active,
        AgentStatus status,
        BigDecimal battery,
        Instant lastSeen,
        BigDecimal currentLatitude,
        BigDecimal currentLongitude,
        String currentAddress,
        BigDecimal currentAccuracy,
        BigDecimal currentSpeed,
        Instant currentLocationUpdatedAt,
        Instant createdAt,
        Instant updatedAt
) {
}

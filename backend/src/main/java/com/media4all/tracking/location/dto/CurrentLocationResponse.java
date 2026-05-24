package com.media4all.tracking.location.dto;

import com.media4all.tracking.agent.AgentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record CurrentLocationResponse(
        String agentId,
        String externalId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String currentAddress,
        BigDecimal accuracy,
        BigDecimal speed,
        BigDecimal battery,
        AgentStatus status,
        Instant lastSeen
) {
}

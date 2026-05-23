package com.media4all.tracking.external.location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.media4all.tracking.agent.AgentStatus;

import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalLocationDto(
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

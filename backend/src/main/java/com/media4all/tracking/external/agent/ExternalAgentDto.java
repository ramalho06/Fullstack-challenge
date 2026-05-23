package com.media4all.tracking.external.agent;

import com.media4all.tracking.agent.AgentRole;
import com.media4all.tracking.agent.AgentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ExternalAgentDto(
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
        Instant createdAt,
        Instant updatedAt
) {
}

package com.media4all.tracking.agent.dto;

import com.media4all.tracking.agent.AgentRole;
import com.media4all.tracking.agent.AgentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Dados públicos de agente")
public record AgentResponse(
        @Schema(example = "string")
        String id,
        @Schema(example = "string")
        String externalId,
        @Schema(example = "string")
        String name,
        AgentRole role,
        @Schema(example = "string")
        String team,
        @Schema(example = "string")
        String phone,
        @Schema(example = "string")
        String email,
        @Schema(example = "true")
        Boolean active,
        AgentStatus status,
        @Schema(example = "0")
        BigDecimal battery,
        @Schema(example = "string")
        Instant lastSeen,
        @Schema(example = "0")
        BigDecimal currentLatitude,
        @Schema(example = "0")
        BigDecimal currentLongitude,
        @Schema(example = "string")
        String currentAddress,
        @Schema(example = "0")
        BigDecimal currentAccuracy,
        @Schema(example = "0")
        BigDecimal currentSpeed,
        @Schema(example = "string")
        Instant currentLocationUpdatedAt,
        @Schema(example = "string")
        Instant createdAt,
        @Schema(example = "string")
        Instant updatedAt
) {
}

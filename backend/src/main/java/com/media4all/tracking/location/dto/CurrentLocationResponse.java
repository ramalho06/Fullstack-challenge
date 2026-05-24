package com.media4all.tracking.location.dto;

import com.media4all.tracking.agent.AgentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Localização atual pública de um agente")
public record CurrentLocationResponse(
        @Schema(example = "string")
        String agentId,
        @Schema(example = "string")
        String externalId,
        @Schema(example = "string")
        String name,
        @Schema(example = "0")
        BigDecimal latitude,
        @Schema(example = "0")
        BigDecimal longitude,
        @Schema(example = "string")
        String currentAddress,
        @Schema(example = "0")
        BigDecimal accuracy,
        @Schema(example = "0")
        BigDecimal speed,
        @Schema(example = "0")
        BigDecimal battery,
        AgentStatus status,
        @Schema(example = "string")
        Instant lastSeen
) {
}

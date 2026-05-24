package com.media4all.tracking.checkin.dto;

import com.media4all.tracking.checkin.CheckInSource;
import com.media4all.tracking.checkin.CheckInType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Dados públicos de check-in")
public record CheckInResponse(
        @Schema(example = "string")
        String id,
        @Schema(example = "string")
        String agentId,
        CheckInType type,
        CheckInSource source,
        @Schema(example = "0")
        BigDecimal latitude,
        @Schema(example = "0")
        BigDecimal longitude,
        @Schema(example = "string")
        String address,
        @Schema(example = "0")
        BigDecimal accuracy,
        @Schema(example = "0")
        BigDecimal speed,
        @Schema(example = "string")
        String notes,
        @Schema(example = "0")
        BigDecimal distanceFromPrevious,
        @Schema(example = "string")
        String externalEventId,
        @Schema(example = "string")
        Instant occurredAt,
        @Schema(example = "string")
        Instant syncedAt
) {
}

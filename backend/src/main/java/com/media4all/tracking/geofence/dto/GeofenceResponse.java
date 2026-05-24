package com.media4all.tracking.geofence.dto;

import com.media4all.tracking.geofence.GeofenceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Dados públicos de geofence")
public record GeofenceResponse(
        @Schema(example = "string")
        String id,
        @Schema(example = "string")
        String externalId,
        @Schema(example = "string")
        String name,
        GeofenceType type,
        @Schema(example = "string")
        String coordinatesJson,
        @Schema(example = "true")
        Boolean alertOnEnter,
        @Schema(example = "true")
        Boolean alertOnExit,
        @Schema(example = "string")
        String assignedTeams,
        @Schema(example = "string")
        Instant syncedAt,
        @Schema(example = "string")
        Instant createdAt,
        @Schema(example = "string")
        Instant updatedAt
) {
}

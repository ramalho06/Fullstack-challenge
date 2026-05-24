package com.media4all.tracking.geofence.dto;

import com.media4all.tracking.geofence.GeofenceType;

import java.time.Instant;

public record GeofenceResponse(
        String id,
        String externalId,
        String name,
        GeofenceType type,
        String coordinatesJson,
        Boolean alertOnEnter,
        Boolean alertOnExit,
        String assignedTeams,
        Instant syncedAt,
        Instant createdAt,
        Instant updatedAt
) {
}

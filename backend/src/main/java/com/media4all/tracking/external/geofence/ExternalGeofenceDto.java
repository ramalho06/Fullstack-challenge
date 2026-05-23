package com.media4all.tracking.external.geofence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.media4all.tracking.geofence.GeofenceType;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalGeofenceDto(
        String id,
        String externalId,
        String name,
        GeofenceType type,
        String coordinatesJson,
        Boolean alertOnEnter,
        Boolean alertOnExit,
        String assignedTeams,
        Instant syncedAt
) {
}

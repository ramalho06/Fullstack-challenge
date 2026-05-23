package com.media4all.tracking.external.geofence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalGeofenceResponse(
        List<ExternalGeofenceDto> data
) {
}

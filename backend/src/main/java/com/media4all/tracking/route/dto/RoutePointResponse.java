package com.media4all.tracking.route.dto;

import com.media4all.tracking.location.LocationSource;

import java.math.BigDecimal;
import java.time.Instant;

public record RoutePointResponse(
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        BigDecimal accuracy,
        BigDecimal speed,
        Instant timestamp,
        LocationSource source,
        BigDecimal distanceFromPreviousMeters
) {
}

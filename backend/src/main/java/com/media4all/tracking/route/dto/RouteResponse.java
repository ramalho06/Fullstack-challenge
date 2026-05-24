package com.media4all.tracking.route.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RouteResponse(
        String agentId,
        LocalDate date,
        BigDecimal totalDistanceMeters,
        List<RoutePointResponse> points
) {
}

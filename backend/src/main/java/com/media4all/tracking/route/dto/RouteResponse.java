package com.media4all.tracking.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Histórico de rota diária de um agente")
public record RouteResponse(
        @Schema(example = "string")
        String agentId,
        @Schema(description = "Data operacional consultada", example = "string")
        LocalDate date,
        @Schema(description = "Distância total calculada com Haversine, em metros", example = "0")
        BigDecimal totalDistanceMeters,
        List<RoutePointResponse> points
) {
}

package com.media4all.tracking.route.dto;

import com.media4all.tracking.location.LocationSource;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Ponto da rota diária")
public record RoutePointResponse(
        @Schema(example = "0")
        BigDecimal latitude,
        @Schema(example = "0")
        BigDecimal longitude,
        @Schema(example = "string")
        String address,
        @Schema(description = "Acurácia GPS em metros", example = "0")
        BigDecimal accuracy,
        @Schema(description = "Velocidade em km/h", example = "0")
        BigDecimal speed,
        @Schema(description = "Timestamp UTC do ponto", example = "string")
        Instant timestamp,
        LocationSource source,
        @Schema(description = "Distância em metros desde o ponto anterior válido", example = "0.00")
        BigDecimal distanceFromPreviousMeters
) {
}

package com.media4all.tracking.checkin.dto;

import com.media4all.tracking.checkin.CheckInType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Dados para registro manual de check-in")
public record CheckInCreateRequest(
        @Schema(description = "ID textual do agente", example = "seed_agent_001")
        @NotBlank
        String agentId,

        @Schema(description = "Tipo do check-in", example = "CHECKIN")
        @NotNull
        CheckInType type,

        @Schema(description = "Latitude em graus decimais", example = "-23.5505")
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        BigDecimal latitude,

        @Schema(description = "Longitude em graus decimais", example = "-46.6333")
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        BigDecimal longitude,

        @Schema(description = "Endereço informado no check-in", example = "Av. Paulista, 1000 - São Paulo, SP")
        @Size(max = 255)
        String address,

        @Schema(description = "Acurácia GPS em metros", example = "10")
        BigDecimal accuracy,

        @Schema(description = "Velocidade em km/h", example = "0")
        BigDecimal speed,

        @Schema(description = "Observações do check-in", example = "Check-in manual")
        String notes,

        @Schema(description = "Data/hora de ocorrência. Se omitido, usa o horário atual.")
        Instant occurredAt
) {
}

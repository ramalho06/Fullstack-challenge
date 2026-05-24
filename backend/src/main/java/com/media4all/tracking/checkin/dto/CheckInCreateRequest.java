package com.media4all.tracking.checkin.dto;

import com.media4all.tracking.checkin.CheckInType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record CheckInCreateRequest(
        @NotBlank
        String agentId,

        @NotNull
        CheckInType type,

        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        BigDecimal latitude,

        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        BigDecimal longitude,

        @Size(max = 255)
        String address,

        BigDecimal accuracy,

        BigDecimal speed,

        String notes,

        Instant occurredAt
) {
}

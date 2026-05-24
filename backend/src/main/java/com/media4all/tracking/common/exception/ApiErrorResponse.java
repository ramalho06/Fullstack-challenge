package com.media4all.tracking.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta padrão de erro da API")
public record ApiErrorResponse(
        ApiError error
) {

    public static ApiErrorResponse of(String code, String message) {
        return of(code, message, null);
    }

    public static ApiErrorResponse of(String code, String message, String details) {
        return new ApiErrorResponse(new ApiError(code, message, details));
    }

    @Schema(description = "Detalhes do erro")
    public record ApiError(
            @Schema(example = "RESOURCE_NOT_FOUND")
            String code,
            @Schema(example = "Agent not found")
            String message,
            @Schema(nullable = true, example = "string")
            String details
    ) {
    }
}

package com.media4all.tracking.sync.dto;

import com.media4all.tracking.sync.SyncExecution;
import com.media4all.tracking.sync.SyncStatus;
import com.media4all.tracking.sync.SyncType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Execução individual de sincronização")
public record SyncExecutionResponse(
        @Schema(example = "0")
        Long id,
        SyncType syncType,
        SyncStatus status,
        @Schema(example = "string")
        Instant startedAt,
        @Schema(example = "string")
        Instant finishedAt,
        @Schema(example = "0")
        Integer itemsProcessed,
        @Schema(example = "0")
        Integer itemsCreated,
        @Schema(example = "0")
        Integer itemsUpdated,
        @Schema(example = "0")
        Integer itemsSkipped,
        @Schema(example = "string")
        String errorMessage,
        @Schema(example = "0")
        Integer httpStatus,
        @Schema(example = "string")
        String syncTokenBefore,
        @Schema(example = "string")
        String syncTokenAfter,
        @Schema(example = "string")
        Instant createdAt,
        @Schema(example = "string")
        Instant updatedAt
) {

    private static final int MAX_ERROR_LENGTH = 500;

    public static SyncExecutionResponse from(SyncExecution execution) {
        return new SyncExecutionResponse(
                execution.getId(),
                execution.getSyncType(),
                execution.getStatus(),
                execution.getStartedAt(),
                execution.getFinishedAt(),
                execution.getItemsProcessed(),
                execution.getItemsCreated(),
                execution.getItemsUpdated(),
                execution.getItemsSkipped(),
                sanitizeErrorMessage(execution.getErrorMessage()),
                execution.getHttpStatus(),
                execution.getSyncTokenBefore(),
                execution.getSyncTokenAfter(),
                execution.getCreatedAt(),
                execution.getUpdatedAt()
        );
    }

    private static String sanitizeErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return null;
        }

        String firstLine = errorMessage.lines().findFirst().orElse(errorMessage).trim();
        if (firstLine.length() <= MAX_ERROR_LENGTH) {
            return firstLine;
        }

        return firstLine.substring(0, MAX_ERROR_LENGTH);
    }
}

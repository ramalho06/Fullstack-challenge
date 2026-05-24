package com.media4all.tracking.sync.dto;

import com.media4all.tracking.sync.SyncExecution;
import com.media4all.tracking.sync.SyncStatus;
import com.media4all.tracking.sync.SyncType;

import java.time.Instant;

public record SyncExecutionResponse(
        Long id,
        SyncType syncType,
        SyncStatus status,
        Instant startedAt,
        Instant finishedAt,
        Integer itemsProcessed,
        Integer itemsCreated,
        Integer itemsUpdated,
        Integer itemsSkipped,
        String errorMessage,
        Integer httpStatus,
        String syncTokenBefore,
        String syncTokenAfter,
        Instant createdAt,
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

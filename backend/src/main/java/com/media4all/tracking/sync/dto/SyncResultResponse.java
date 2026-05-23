package com.media4all.tracking.sync.dto;

import com.media4all.tracking.sync.SyncExecution;

import java.time.Instant;

public record SyncResultResponse(
        String syncType,
        String status,
        int processed,
        int created,
        int updated,
        int skipped,
        Instant startedAt,
        Instant finishedAt
) {

    public static SyncResultResponse from(SyncExecution execution) {
        return new SyncResultResponse(
                execution.getSyncType().name(),
                execution.getStatus().name(),
                valueOrZero(execution.getItemsProcessed()),
                valueOrZero(execution.getItemsCreated()),
                valueOrZero(execution.getItemsUpdated()),
                valueOrZero(execution.getItemsSkipped()),
                execution.getStartedAt(),
                execution.getFinishedAt()
        );
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}

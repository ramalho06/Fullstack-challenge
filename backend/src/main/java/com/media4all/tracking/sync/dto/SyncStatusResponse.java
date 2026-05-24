package com.media4all.tracking.sync.dto;

import com.media4all.tracking.sync.OperationalSyncStatus;

import java.time.Instant;
import java.util.List;

public record SyncStatusResponse(
        OperationalSyncStatus overallStatus,
        Instant lastSuccessfulSyncAt,
        Instant lastFailedSyncAt,
        long totalExecutions,
        long totalFailures,
        List<SyncTypeStatusResponse> syncs
) {
}

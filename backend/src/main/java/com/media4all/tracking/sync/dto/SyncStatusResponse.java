package com.media4all.tracking.sync.dto;

import com.media4all.tracking.sync.OperationalSyncStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Resumo operacional das sincronizações")
public record SyncStatusResponse(
        @Schema(description = "Status consolidado das últimas execuções", example = "HEALTHY")
        OperationalSyncStatus overallStatus,
        Instant lastSuccessfulSyncAt,
        Instant lastFailedSyncAt,
        long totalExecutions,
        long totalFailures,
        List<SyncTypeStatusResponse> syncs
) {
}

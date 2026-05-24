package com.media4all.tracking.sync.dto;

import com.media4all.tracking.sync.SyncType;

public record SyncTypeStatusResponse(
        SyncType syncType,
        boolean schedulerEnabled,
        long fixedDelayMs,
        long initialDelayMs,
        SyncExecutionResponse lastExecution
) {

    public static SyncTypeStatusResponse of(SchedulerInfoResponse schedulerInfo, SyncExecutionResponse lastExecution) {
        return new SyncTypeStatusResponse(
                schedulerInfo.syncType(),
                schedulerInfo.schedulerEnabled(),
                schedulerInfo.fixedDelayMs(),
                schedulerInfo.initialDelayMs(),
                lastExecution
        );
    }
}

package com.media4all.tracking.sync.dto;

import com.media4all.tracking.sync.SyncType;

public record SchedulerInfoResponse(
        SyncType syncType,
        boolean schedulerEnabled,
        long fixedDelayMs,
        long initialDelayMs
) {
}

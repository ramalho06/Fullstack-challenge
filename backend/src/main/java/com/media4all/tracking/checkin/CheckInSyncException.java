package com.media4all.tracking.checkin;

public class CheckInSyncException extends RuntimeException {

    private final Long syncExecutionId;

    public CheckInSyncException(String message, Long syncExecutionId, Throwable cause) {
        super(message, cause);
        this.syncExecutionId = syncExecutionId;
    }

    public Long getSyncExecutionId() {
        return syncExecutionId;
    }
}

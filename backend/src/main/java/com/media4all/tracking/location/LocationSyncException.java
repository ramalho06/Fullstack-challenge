package com.media4all.tracking.location;

public class LocationSyncException extends RuntimeException {

    private final Long syncExecutionId;

    public LocationSyncException(String message, Long syncExecutionId, Throwable cause) {
        super(message, cause);
        this.syncExecutionId = syncExecutionId;
    }

    public Long getSyncExecutionId() {
        return syncExecutionId;
    }
}

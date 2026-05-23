package com.media4all.tracking.geofence;

public class GeofenceSyncException extends RuntimeException {

    private final Long syncExecutionId;

    public GeofenceSyncException(String message, Long syncExecutionId, Throwable cause) {
        super(message, cause);
        this.syncExecutionId = syncExecutionId;
    }

    public Long getSyncExecutionId() {
        return syncExecutionId;
    }
}

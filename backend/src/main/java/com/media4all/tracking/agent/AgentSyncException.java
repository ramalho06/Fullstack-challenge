package com.media4all.tracking.agent;

public class AgentSyncException extends RuntimeException {

    private final Long syncExecutionId;

    public AgentSyncException(String message, Long syncExecutionId, Throwable cause) {
        super(message, cause);
        this.syncExecutionId = syncExecutionId;
    }

    public Long getSyncExecutionId() {
        return syncExecutionId;
    }
}

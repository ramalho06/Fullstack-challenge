package com.media4all.tracking.sync;

import com.media4all.tracking.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "sync_executions",
        indexes = {
                @Index(name = "idx_sync_executions_sync_type", columnList = "sync_type"),
                @Index(name = "idx_sync_executions_status", columnList = "status"),
                @Index(name = "idx_sync_executions_started_at", columnList = "started_at")
        }
)
public class SyncExecution extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SyncType syncType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SyncStatus status;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant finishedAt;

    private Integer itemsProcessed;

    private Integer itemsCreated;

    private Integer itemsUpdated;

    private Integer itemsSkipped;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Integer httpStatus;

    @Column(length = 1024)
    private String syncTokenBefore;

    @Column(length = 1024)
    private String syncTokenAfter;

    public Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }

    public SyncType getSyncType() {
        return syncType;
    }

    public void setSyncType(SyncType syncType) {
        this.syncType = syncType;
    }

    public SyncStatus getStatus() {
        return status;
    }

    public void setStatus(SyncStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Integer getItemsProcessed() {
        return itemsProcessed;
    }

    public void setItemsProcessed(Integer itemsProcessed) {
        this.itemsProcessed = itemsProcessed;
    }

    public Integer getItemsCreated() {
        return itemsCreated;
    }

    public void setItemsCreated(Integer itemsCreated) {
        this.itemsCreated = itemsCreated;
    }

    public Integer getItemsUpdated() {
        return itemsUpdated;
    }

    public void setItemsUpdated(Integer itemsUpdated) {
        this.itemsUpdated = itemsUpdated;
    }

    public Integer getItemsSkipped() {
        return itemsSkipped;
    }

    public void setItemsSkipped(Integer itemsSkipped) {
        this.itemsSkipped = itemsSkipped;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }
}

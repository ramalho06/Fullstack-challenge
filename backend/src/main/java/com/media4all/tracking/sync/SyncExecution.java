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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
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
}

package com.media4all.tracking.sync;

import com.media4all.tracking.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "sync_states",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_sync_states_sync_type", columnNames = "sync_type")
        }
)
public class SyncState extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SyncType syncType;

    @Column(length = 1024)
    private String lastSyncToken;

    private Instant lastSuccessfulSyncAt;

    private Instant lastAttemptAt;
}

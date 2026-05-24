package com.media4all.tracking.sync;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SyncExecutionRepository extends JpaRepository<SyncExecution, Long>, JpaSpecificationExecutor<SyncExecution> {

    Optional<SyncExecution> findTopBySyncTypeOrderByStartedAtDesc(SyncType syncType);

    Optional<SyncExecution> findTopByStatusOrderByFinishedAtDesc(SyncStatus status);

    long countByStatus(SyncStatus status);
}

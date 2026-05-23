package com.media4all.tracking.sync;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncExecutionRepository extends JpaRepository<SyncExecution, Long> {
}

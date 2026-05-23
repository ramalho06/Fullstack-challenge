package com.media4all.tracking.sync;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SyncStateRepository extends JpaRepository<SyncState, Long> {

    Optional<SyncState> findBySyncType(SyncType syncType);
}

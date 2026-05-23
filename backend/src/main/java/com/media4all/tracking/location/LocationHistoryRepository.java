package com.media4all.tracking.location;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {

    boolean existsByAgentIdAndRecordedAtAndSource(String agentId, Instant recordedAt, LocationSource source);

    Optional<LocationHistory> findByAgentIdAndRecordedAtAndSource(
            String agentId,
            Instant recordedAt,
            LocationSource source
    );
}

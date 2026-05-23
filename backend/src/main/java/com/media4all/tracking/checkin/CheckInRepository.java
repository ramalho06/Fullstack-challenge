package com.media4all.tracking.checkin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, String> {

    boolean existsByExternalEventId(String externalEventId);

    Optional<CheckIn> findByExternalEventId(String externalEventId);

    List<CheckIn> findByAgentIdOrderByOccurredAtAsc(String agentId);
}

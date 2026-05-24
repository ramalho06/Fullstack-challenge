package com.media4all.tracking.checkin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, String>, JpaSpecificationExecutor<CheckIn> {

    boolean existsByExternalEventId(String externalEventId);

    Optional<CheckIn> findByExternalEventId(String externalEventId);

    List<CheckIn> findByAgentIdOrderByOccurredAtAsc(String agentId);
}

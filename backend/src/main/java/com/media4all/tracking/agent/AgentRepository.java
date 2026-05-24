package com.media4all.tracking.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, String>, JpaSpecificationExecutor<Agent> {

    Optional<Agent> findByExternalId(String externalId);

    boolean existsByExternalId(String externalId);
}

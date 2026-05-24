package com.media4all.tracking.location;

import com.media4all.tracking.agent.Agent;
import com.media4all.tracking.agent.AgentRepository;
import com.media4all.tracking.agent.AgentStatus;
import com.media4all.tracking.common.exception.ResourceNotFoundException;
import com.media4all.tracking.location.dto.CurrentLocationResponse;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocationQueryService {

    private final AgentRepository agentRepository;

    public LocationQueryService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    @Transactional(readOnly = true)
    public List<CurrentLocationResponse> findCurrentLocations(Boolean onlineOnly, Boolean active) {
        Boolean activeFilter = active == null ? Boolean.TRUE : active;
        return agentRepository.findAll(withLocationFilters(onlineOnly, activeFilter))
                .stream()
                .map(this::toCurrentLocationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CurrentLocationResponse findCurrentLocationByAgentId(String agentId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent", agentId));
        return toCurrentLocationResponse(agent);
    }

    private Specification<Agent> withLocationFilters(Boolean onlineOnly, Boolean active) {
        Specification<Agent> activeSpecification = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("active"), active);

        return Specification.where(activeSpecification)
                .and((root, query, criteriaBuilder) ->
                        Boolean.TRUE.equals(onlineOnly)
                                ? criteriaBuilder.equal(root.get("status"), AgentStatus.ONLINE)
                                : null);
    }

    private CurrentLocationResponse toCurrentLocationResponse(Agent agent) {
        return new CurrentLocationResponse(
                agent.getId(),
                agent.getExternalId(),
                agent.getName(),
                agent.getCurrentLatitude(),
                agent.getCurrentLongitude(),
                agent.getCurrentAddress(),
                agent.getCurrentAccuracy(),
                agent.getCurrentSpeed(),
                agent.getBattery(),
                agent.getStatus(),
                agent.getLastSeen()
        );
    }
}

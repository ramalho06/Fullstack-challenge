package com.media4all.tracking.agent;

import com.media4all.tracking.external.agent.ExternalAgentDto;
import com.media4all.tracking.agent.dto.AgentResponse;
import org.springframework.stereotype.Component;

@Component
public class AgentMapper {

    public Agent createFromExternal(ExternalAgentDto dto) {
        Agent agent = new Agent();
        agent.setId(dto.id());
        updateFromExternal(agent, dto);
        return agent;
    }

    public void updateFromExternal(Agent agent, ExternalAgentDto dto) {
        agent.setExternalId(dto.externalId());
        agent.setName(dto.name());
        agent.setRole(dto.role());
        agent.setTeam(dto.team());
        agent.setPhone(dto.phone());
        agent.setEmail(dto.email());
        agent.setActive(dto.active());
        agent.setStatus(dto.status());
        agent.setBattery(dto.battery());
        agent.setLastSeen(dto.lastSeen());
        agent.setExternalCreatedAt(dto.createdAt());
        agent.setExternalUpdatedAt(dto.updatedAt());
    }

    public AgentResponse toResponse(Agent agent) {
        return new AgentResponse(
                agent.getId(),
                agent.getExternalId(),
                agent.getName(),
                agent.getRole(),
                agent.getTeam(),
                agent.getPhone(),
                agent.getEmail(),
                agent.getActive(),
                agent.getStatus(),
                agent.getBattery(),
                agent.getLastSeen(),
                agent.getCurrentLatitude(),
                agent.getCurrentLongitude(),
                agent.getCurrentAddress(),
                agent.getCurrentAccuracy(),
                agent.getCurrentSpeed(),
                agent.getCurrentLocationUpdatedAt(),
                agent.getCreatedAt(),
                agent.getUpdatedAt()
        );
    }
}

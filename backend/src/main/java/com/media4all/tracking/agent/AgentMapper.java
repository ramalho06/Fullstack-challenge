package com.media4all.tracking.agent;

import com.media4all.tracking.external.agent.ExternalAgentDto;
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
}

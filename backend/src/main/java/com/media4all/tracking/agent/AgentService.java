package com.media4all.tracking.agent;

import com.media4all.tracking.agent.dto.AgentCreateRequest;
import com.media4all.tracking.agent.dto.AgentResponse;
import com.media4all.tracking.agent.dto.AgentUpdateRequest;
import com.media4all.tracking.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class AgentService {

    private static final String LOCAL_AGENT_PREFIX = "local_agent_";
    private static final String LOCAL_EXTERNAL_AGENT_PREFIX = "local-ext-agent_";

    private final AgentRepository agentRepository;
    private final AgentMapper agentMapper;

    public AgentService(AgentRepository agentRepository, AgentMapper agentMapper) {
        this.agentRepository = agentRepository;
        this.agentMapper = agentMapper;
    }

    @Transactional(readOnly = true)
    public Page<AgentResponse> findAgents(
            Boolean active,
            AgentStatus status,
            AgentRole role,
            String team,
            String search,
            Pageable pageable
    ) {
        return agentRepository.findAll(withFilters(active, status, role, team, search), pageable)
                .map(agentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AgentResponse findById(String id) {
        return agentMapper.toResponse(findAgent(id));
    }

    @Transactional
    public AgentResponse create(AgentCreateRequest request) {
        String uuid = UUID.randomUUID().toString().replace("-", "");

        Agent agent = new Agent();
        agent.setId(LOCAL_AGENT_PREFIX + uuid);
        agent.setExternalId(LOCAL_EXTERNAL_AGENT_PREFIX + uuid);
        agent.setName(request.name());
        agent.setRole(request.role());
        agent.setTeam(request.team());
        agent.setPhone(request.phone());
        agent.setEmail(request.email());
        agent.setActive(request.active() == null ? Boolean.TRUE : request.active());
        agent.setStatus(request.status() == null ? AgentStatus.OFFLINE : request.status());

        return agentMapper.toResponse(agentRepository.save(agent));
    }

    @Transactional
    public AgentResponse update(String id, AgentUpdateRequest request) {
        Agent agent = findAgent(id);
        agent.setName(request.name());
        agent.setRole(request.role());
        agent.setTeam(request.team());
        agent.setPhone(request.phone());
        agent.setEmail(request.email());
        agent.setActive(request.active());
        agent.setStatus(request.status());
        return agentMapper.toResponse(agentRepository.saveAndFlush(agent));
    }

    @Transactional
    public void softDelete(String id) {
        Agent agent = findAgent(id);
        agent.setActive(false);
        agent.setStatus(AgentStatus.OFFLINE);
    }

    private Agent findAgent(String id) {
        return agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent", id));
    }

    private Specification<Agent> withFilters(
            Boolean active,
            AgentStatus status,
            AgentRole role,
            String team,
            String search
    ) {
        return Specification.where(activeEquals(active))
                .and(statusEquals(status))
                .and(roleEquals(role))
                .and(teamEquals(team))
                .and(searchMatches(search));
    }

    private Specification<Agent> activeEquals(Boolean active) {
        return (root, query, criteriaBuilder) ->
                active == null ? null : criteriaBuilder.equal(root.get("active"), active);
    }

    private Specification<Agent> statusEquals(AgentStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    private Specification<Agent> roleEquals(AgentRole role) {
        return (root, query, criteriaBuilder) ->
                role == null ? null : criteriaBuilder.equal(root.get("role"), role);
    }

    private Specification<Agent> teamEquals(String team) {
        return (root, query, criteriaBuilder) ->
                StringUtils.hasText(team) ? criteriaBuilder.equal(root.get("team"), team) : null;
    }

    private Specification<Agent> searchMatches(String search) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(search)) {
                return null;
            }

            String pattern = "%" + search.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("team")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), pattern)
            );
        };
    }
}

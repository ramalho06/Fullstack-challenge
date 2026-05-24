package com.media4all.tracking.agent;

import com.media4all.tracking.agent.dto.AgentCreateRequest;
import com.media4all.tracking.agent.dto.AgentResponse;
import com.media4all.tracking.agent.dto.AgentUpdateRequest;
import com.media4all.tracking.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentServiceTest {

    private final Map<String, Agent> agentsById = new HashMap<>();
    private AgentService service;

    @BeforeEach
    void setUp() {
        service = new AgentService(agentRepositoryProxy(), new AgentMapper());
    }

    @Test
    void createsLocalAgentWithGeneratedIdAndExternalId() {
        AgentResponse response = service.create(new AgentCreateRequest(
                "Local Agent",
                AgentRole.TECHNICIAN,
                "Alpha",
                "+5511999990000",
                "local@example.com",
                null,
                null
        ));

        assertThat(response.id()).startsWith("local_agent_");
        assertThat(response.externalId()).startsWith("local-ext-agent_");
        assertThat(response.active()).isTrue();
        assertThat(response.status()).isEqualTo(AgentStatus.OFFLINE);
        assertThat(agentsById).hasSize(1);
    }

    @Test
    void updatesOnlyAllowedFields() {
        Agent agent = existingAgent("seed_agent_001");
        agent.setBattery(BigDecimal.valueOf(70));
        agent.setLastSeen(Instant.parse("2026-05-22T06:00:00Z"));
        agent.setCurrentLatitude(BigDecimal.valueOf(-23.55));
        agentsById.put(agent.getId(), agent);

        AgentResponse response = service.update(agent.getId(), new AgentUpdateRequest(
                "Updated",
                AgentRole.VENDOR,
                "Beta",
                "+5511888880000",
                "updated@example.com",
                true,
                AgentStatus.ONLINE
        ));

        assertThat(response.name()).isEqualTo("Updated");
        assertThat(response.role()).isEqualTo(AgentRole.VENDOR);
        assertThat(response.team()).isEqualTo("Beta");
        assertThat(response.phone()).isEqualTo("+5511888880000");
        assertThat(response.email()).isEqualTo("updated@example.com");
        assertThat(response.battery()).isEqualByComparingTo("70");
        assertThat(response.lastSeen()).isEqualTo(Instant.parse("2026-05-22T06:00:00Z"));
        assertThat(response.currentLatitude()).isEqualByComparingTo("-23.55");
    }

    @Test
    void softDeleteSetsInactiveAndOffline() {
        Agent agent = existingAgent("seed_agent_001");
        agent.setActive(true);
        agent.setStatus(AgentStatus.ONLINE);
        agentsById.put(agent.getId(), agent);

        service.softDelete(agent.getId());

        assertThat(agent.getActive()).isFalse();
        assertThat(agent.getStatus()).isEqualTo(AgentStatus.OFFLINE);
        assertThat(agentsById).containsKey(agent.getId());
    }

    @Test
    void throwsNotFoundWhenAgentDoesNotExist() {
        assertThatThrownBy(() -> service.findById("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Agent not found");
    }

    private Agent existingAgent(String id) {
        Agent agent = new Agent();
        agent.setId(id);
        agent.setExternalId("ext-" + id);
        agent.setName("Agent");
        agent.setActive(true);
        agent.setStatus(AgentStatus.OFFLINE);
        return agent;
    }

    private AgentRepository agentRepositoryProxy() {
        return (AgentRepository) Proxy.newProxyInstance(
                AgentRepository.class.getClassLoader(),
                new Class[]{AgentRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.ofNullable(agentsById.get(args[0]));
                    case "save", "saveAndFlush" -> {
                        Agent agent = (Agent) args[0];
                        agentsById.put(agent.getId(), agent);
                        yield agent;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }
}

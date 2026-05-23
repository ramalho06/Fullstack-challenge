package com.media4all.tracking.agent;

import com.media4all.tracking.external.ExternalApiException;
import com.media4all.tracking.external.agent.ExternalAgentDto;
import com.media4all.tracking.external.agent.ExternalAgentGateway;
import com.media4all.tracking.sync.SyncExecution;
import com.media4all.tracking.sync.SyncExecutionRepository;
import com.media4all.tracking.sync.SyncStatus;
import com.media4all.tracking.sync.SyncType;
import com.media4all.tracking.sync.dto.SyncResultResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentSyncServiceTest {

    private final Map<String, Agent> agentsByExternalId = new HashMap<>();
    private final Map<Long, SyncExecution> executionsById = new HashMap<>();
    private final AtomicLong executionIds = new AtomicLong(1);

    private ExternalAgentGateway externalAgentGateway;
    private AgentRepository agentRepository;
    private SyncExecutionRepository syncExecutionRepository;
    private AgentSyncService service;

    @BeforeEach
    void setUp() {
        externalAgentGateway = new ExternalAgentGatewayFake();
        agentRepository = agentRepositoryProxy();
        syncExecutionRepository = syncExecutionRepositoryProxy();

        service = new AgentSyncService(
                externalAgentGateway,
                agentRepository,
                new AgentMapper(),
                syncExecutionRepository,
                new ImmediateTransactionTemplate()
        );
    }

    @Test
    void createsNewAgentsAndRegistersSuccess() {
        ((ExternalAgentGatewayFake) externalAgentGateway).agents =
                List.of(agentDto("seed_agent_001", "ext-agent-001", "Carlos"));

        SyncResultResponse response = service.syncAgents();

        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.processed()).isEqualTo(1);
        assertThat(response.created()).isEqualTo(1);
        assertThat(response.updated()).isZero();
        assertThat(agentsByExternalId).hasSize(1);
        assertThat(agentsByExternalId.get("ext-agent-001").getId()).isEqualTo("seed_agent_001");

        SyncExecution execution = executionsById.get(1L);
        assertThat(execution.getSyncType()).isEqualTo(SyncType.AGENTS);
        assertThat(execution.getStatus()).isEqualTo(SyncStatus.SUCCESS);
        assertThat(execution.getItemsCreated()).isEqualTo(1);
    }

    @Test
    void updatesExistingAgentsByExternalId() {
        Agent existing = existingAgent("seed_agent_001", "ext-agent-001", "Old Name");
        agentsByExternalId.put(existing.getExternalId(), existing);

        ((ExternalAgentGatewayFake) externalAgentGateway).agents =
                List.of(agentDto("seed_agent_001", "ext-agent-001", "New Name"));

        SyncResultResponse response = service.syncAgents();

        assertThat(response.created()).isZero();
        assertThat(response.updated()).isEqualTo(1);
        assertThat(agentsByExternalId).hasSize(1);
        assertThat(agentsByExternalId.get("ext-agent-001").getName()).isEqualTo("New Name");
    }

    @Test
    void secondRunDoesNotDuplicateAgents() {
        ((ExternalAgentGatewayFake) externalAgentGateway).agents =
                List.of(agentDto("seed_agent_001", "ext-agent-001", "Carlos"));

        SyncResultResponse first = service.syncAgents();
        SyncResultResponse second = service.syncAgents();

        assertThat(first.created()).isEqualTo(1);
        assertThat(second.created()).isZero();
        assertThat(second.updated()).isEqualTo(1);
        assertThat(agentsByExternalId).hasSize(1);
    }

    @Test
    void registersFailedExecutionWhenExternalClientFails() {
        ((ExternalAgentGatewayFake) externalAgentGateway).exception =
                new ExternalApiException("External API returned HTTP 503", 503, "unavailable");

        assertThatThrownBy(() -> service.syncAgents())
                .isInstanceOf(ExternalApiException.class);

        SyncExecution execution = executionsById.get(1L);
        assertThat(execution.getStatus()).isEqualTo(SyncStatus.FAILED);
        assertThat(execution.getHttpStatus()).isEqualTo(503);
    }

    @Test
    void detectsConflictWhenExternalIdPointsToDifferentId() {
        Agent existing = existingAgent("seed_agent_001", "ext-agent-001", "Carlos");
        agentsByExternalId.put(existing.getExternalId(), existing);

        ((ExternalAgentGatewayFake) externalAgentGateway).agents =
                List.of(agentDto("seed_agent_999", "ext-agent-001", "Carlos"));

        assertThatThrownBy(() -> service.syncAgents())
                .isInstanceOf(AgentSyncException.class)
                .hasMessage("Failed to synchronize agents");

        SyncExecution execution = executionsById.get(1L);
        assertThat(execution.getStatus()).isEqualTo(SyncStatus.FAILED);
    }

    private Agent existingAgent(String id, String externalId, String name) {
        Agent agent = new Agent();
        agent.setId(id);
        agent.setExternalId(externalId);
        agent.setName(name);
        agent.setActive(true);
        agent.setStatus(AgentStatus.ONLINE);
        return agent;
    }

    private ExternalAgentDto agentDto(String id, String externalId, String name) {
        return new ExternalAgentDto(
                id,
                externalId,
                name,
                AgentRole.TECHNICIAN,
                "Alpha",
                "+5511999990001",
                "agent@example.com",
                true,
                AgentStatus.ONLINE,
                BigDecimal.valueOf(85),
                Instant.parse("2026-05-22T06:00:00Z"),
                Instant.parse("2026-05-23T02:35:33Z"),
                Instant.parse("2026-05-23T02:35:50Z")
        );
    }

    private AgentRepository agentRepositoryProxy() {
        return (AgentRepository) Proxy.newProxyInstance(
                AgentRepository.class.getClassLoader(),
                new Class[]{AgentRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByExternalId" -> Optional.ofNullable(agentsByExternalId.get(args[0]));
                    case "save" -> {
                        Agent agent = (Agent) args[0];
                        agentsByExternalId.put(agent.getExternalId(), agent);
                        yield agent;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private SyncExecutionRepository syncExecutionRepositoryProxy() {
        return (SyncExecutionRepository) Proxy.newProxyInstance(
                SyncExecutionRepository.class.getClassLoader(),
                new Class[]{SyncExecutionRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "save" -> {
                        SyncExecution execution = (SyncExecution) args[0];
                        if (execution.getId() == null) {
                            ReflectionTestUtils.setField(execution, "id", executionIds.getAndIncrement());
                        }
                        executionsById.put(execution.getId(), execution);
                        yield execution;
                    }
                    case "findById" -> Optional.ofNullable(executionsById.get(args[0]));
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private static class ExternalAgentGatewayFake implements ExternalAgentGateway {
        private List<ExternalAgentDto> agents = List.of();
        private RuntimeException exception;

        @Override
        public List<ExternalAgentDto> fetchAllAgents() {
            if (exception != null) {
                throw exception;
            }

            return agents;
        }
    }

    private static class ImmediateTransactionTemplate extends TransactionTemplate {
        @Override
        public <T> T execute(TransactionCallback<T> action) {
            TransactionStatus status = new SimpleTransactionStatus();
            return action.doInTransaction(status);
        }
    }
}

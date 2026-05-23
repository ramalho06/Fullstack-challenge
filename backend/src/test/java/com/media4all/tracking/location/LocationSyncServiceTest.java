package com.media4all.tracking.location;

import com.media4all.tracking.agent.Agent;
import com.media4all.tracking.agent.AgentRepository;
import com.media4all.tracking.agent.AgentStatus;
import com.media4all.tracking.external.ExternalApiException;
import com.media4all.tracking.external.location.ExternalLocationDto;
import com.media4all.tracking.external.location.ExternalLocationGateway;
import com.media4all.tracking.sync.SyncExecution;
import com.media4all.tracking.sync.SyncExecutionRepository;
import com.media4all.tracking.sync.SyncStatus;
import com.media4all.tracking.sync.SyncType;
import com.media4all.tracking.sync.dto.SyncResultResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocationSyncServiceTest {

    private final Map<String, Agent> agentsById = new HashMap<>();
    private final Set<String> historyKeys = new HashSet<>();
    private final Map<Long, SyncExecution> executionsById = new HashMap<>();
    private final AtomicLong executionIds = new AtomicLong(1);

    private ExternalLocationGateway externalLocationGateway;
    private LocationSyncService service;

    @BeforeEach
    void setUp() {
        externalLocationGateway = new ExternalLocationGatewayFake();

        service = new LocationSyncService(
                externalLocationGateway,
                agentRepositoryProxy(),
                locationHistoryRepositoryProxy(),
                new LocationMapper(),
                syncExecutionRepositoryProxy(),
                new ImmediateTransactionTemplate()
        );
    }

    @Test
    void updatesExistingAgentWithValidLocation() {
        Agent agent = existingAgent("seed_agent_001");
        agentsById.put(agent.getId(), agent);
        ((ExternalLocationGatewayFake) externalLocationGateway).locations =
                List.of(locationDto("seed_agent_001", BigDecimal.valueOf(10)));

        SyncResultResponse response = service.syncLocations();

        assertThat(response.updated()).isEqualTo(1);
        assertThat(agent.getCurrentLatitude()).isEqualByComparingTo("-23.5610000");
        assertThat(agent.getCurrentLongitude()).isEqualByComparingTo("-46.6550000");
        assertThat(agent.getCurrentAddress()).isEqualTo("Rua Teste, 100");
        assertThat(agent.getCurrentAccuracy()).isEqualByComparingTo("10");
        assertThat(agent.getCurrentSpeed()).isEqualByComparingTo("20.5");
        assertThat(agent.getBattery()).isEqualByComparingTo("78");
        assertThat(agent.getStatus()).isEqualTo(AgentStatus.ONLINE);
        assertThat(agent.getLastSeen()).isEqualTo(Instant.parse("2026-05-22T06:15:00Z"));
        assertThat(agent.getCurrentLocationUpdatedAt()).isEqualTo(Instant.parse("2026-05-22T06:15:00Z"));
    }

    @Test
    void createsLocationHistoryForValidLocation() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        ((ExternalLocationGatewayFake) externalLocationGateway).locations =
                List.of(locationDto("seed_agent_001", BigDecimal.valueOf(10)));

        SyncResultResponse response = service.syncLocations();

        assertThat(response.created()).isEqualTo(1);
        assertThat(historyKeys).contains("seed_agent_001|2026-05-22T06:15:00Z|GPS_SYNC");
    }

    @Test
    void doesNotDuplicateHistoryWhenLogicalKeyAlreadyExists() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        historyKeys.add("seed_agent_001|2026-05-22T06:15:00Z|GPS_SYNC");
        ((ExternalLocationGatewayFake) externalLocationGateway).locations =
                List.of(locationDto("seed_agent_001", BigDecimal.valueOf(10)));

        SyncResultResponse response = service.syncLocations();

        assertThat(response.created()).isZero();
        assertThat(response.updated()).isEqualTo(1);
        assertThat(historyKeys).hasSize(1);
    }

    @Test
    void skipsLocationWhenAccuracyIsGreaterThanFifty() {
        Agent agent = existingAgent("seed_agent_001");
        agentsById.put(agent.getId(), agent);
        ((ExternalLocationGatewayFake) externalLocationGateway).locations =
                List.of(locationDto("seed_agent_001", BigDecimal.valueOf(51)));

        SyncResultResponse response = service.syncLocations();

        assertThat(response.skipped()).isEqualTo(1);
        assertThat(response.updated()).isZero();
        assertThat(response.created()).isZero();
        assertThat(agent.getCurrentLatitude()).isNull();
    }

    @Test
    void acceptsLocationWhenAccuracyIsNull() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        ((ExternalLocationGatewayFake) externalLocationGateway).locations =
                List.of(locationDto("seed_agent_001", null));

        SyncResultResponse response = service.syncLocations();

        assertThat(response.skipped()).isZero();
        assertThat(response.updated()).isEqualTo(1);
        assertThat(response.created()).isEqualTo(1);
    }

    @Test
    void skipsLocationWhenAgentDoesNotExist() {
        ((ExternalLocationGatewayFake) externalLocationGateway).locations =
                List.of(locationDto("seed_agent_999", BigDecimal.valueOf(10)));

        SyncResultResponse response = service.syncLocations();

        assertThat(response.skipped()).isEqualTo(1);
        assertThat(response.updated()).isZero();
        assertThat(response.created()).isZero();
    }

    @Test
    void skipsLocationWhenLatitudeIsNull() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        ((ExternalLocationGatewayFake) externalLocationGateway).locations =
                List.of(new ExternalLocationDto(
                        "seed_agent_001",
                        "ext-agent-001",
                        "Ana",
                        null,
                        BigDecimal.valueOf(-46.6550000),
                        "Rua Teste, 100",
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(20.5),
                        BigDecimal.valueOf(78),
                        AgentStatus.ONLINE,
                        Instant.parse("2026-05-22T06:15:00Z")
                ));

        SyncResultResponse response = service.syncLocations();

        assertThat(response.skipped()).isEqualTo(1);
        assertThat(response.updated()).isZero();
    }

    @Test
    void skipsLocationWhenLongitudeIsNull() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        ((ExternalLocationGatewayFake) externalLocationGateway).locations =
                List.of(new ExternalLocationDto(
                        "seed_agent_001",
                        "ext-agent-001",
                        "Ana",
                        BigDecimal.valueOf(-23.5610000),
                        null,
                        "Rua Teste, 100",
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(20.5),
                        BigDecimal.valueOf(78),
                        AgentStatus.ONLINE,
                        Instant.parse("2026-05-22T06:15:00Z")
                ));

        SyncResultResponse response = service.syncLocations();

        assertThat(response.skipped()).isEqualTo(1);
        assertThat(response.updated()).isZero();
    }

    @Test
    void skipsLocationWhenLastSeenIsNull() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        ((ExternalLocationGatewayFake) externalLocationGateway).locations =
                List.of(new ExternalLocationDto(
                        "seed_agent_001",
                        "ext-agent-001",
                        "Ana",
                        BigDecimal.valueOf(-23.5610000),
                        BigDecimal.valueOf(-46.6550000),
                        "Rua Teste, 100",
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(20.5),
                        BigDecimal.valueOf(78),
                        AgentStatus.ONLINE,
                        null
                ));

        SyncResultResponse response = service.syncLocations();

        assertThat(response.skipped()).isEqualTo(1);
        assertThat(response.updated()).isZero();
    }

    @Test
    void registersSuccessfulExecution() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        ((ExternalLocationGatewayFake) externalLocationGateway).locations =
                List.of(locationDto("seed_agent_001", BigDecimal.valueOf(10)));

        service.syncLocations();

        SyncExecution execution = executionsById.get(1L);
        assertThat(execution.getSyncType()).isEqualTo(SyncType.LOCATIONS);
        assertThat(execution.getStatus()).isEqualTo(SyncStatus.SUCCESS);
        assertThat(execution.getItemsProcessed()).isEqualTo(1);
        assertThat(execution.getItemsCreated()).isEqualTo(1);
        assertThat(execution.getItemsUpdated()).isEqualTo(1);
        assertThat(execution.getItemsSkipped()).isZero();
    }

    @Test
    void registersFailedExecutionWhenExternalClientFails() {
        ((ExternalLocationGatewayFake) externalLocationGateway).exception =
                new ExternalApiException("External API returned HTTP 503", 503, "unavailable");

        assertThatThrownBy(() -> service.syncLocations())
                .isInstanceOf(ExternalApiException.class);

        SyncExecution execution = executionsById.get(1L);
        assertThat(execution.getSyncType()).isEqualTo(SyncType.LOCATIONS);
        assertThat(execution.getStatus()).isEqualTo(SyncStatus.FAILED);
        assertThat(execution.getHttpStatus()).isEqualTo(503);
    }

    private Agent existingAgent(String id) {
        Agent agent = new Agent();
        agent.setId(id);
        agent.setExternalId("ext-" + id);
        agent.setName("Ana");
        agent.setActive(true);
        agent.setStatus(AgentStatus.OFFLINE);
        return agent;
    }

    private ExternalLocationDto locationDto(String agentId, BigDecimal accuracy) {
        return new ExternalLocationDto(
                agentId,
                "ext-agent-001",
                "Ana",
                BigDecimal.valueOf(-23.5610000),
                BigDecimal.valueOf(-46.6550000),
                "Rua Teste, 100",
                accuracy,
                BigDecimal.valueOf(20.5),
                BigDecimal.valueOf(78),
                AgentStatus.ONLINE,
                Instant.parse("2026-05-22T06:15:00Z")
        );
    }

    private AgentRepository agentRepositoryProxy() {
        return (AgentRepository) Proxy.newProxyInstance(
                AgentRepository.class.getClassLoader(),
                new Class[]{AgentRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.ofNullable(agentsById.get(args[0]));
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private LocationHistoryRepository locationHistoryRepositoryProxy() {
        return (LocationHistoryRepository) Proxy.newProxyInstance(
                LocationHistoryRepository.class.getClassLoader(),
                new Class[]{LocationHistoryRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "existsByAgentIdAndRecordedAtAndSource" -> historyKeys.contains(key(
                            (String) args[0],
                            (Instant) args[1],
                            (LocationSource) args[2]
                    ));
                    case "save" -> {
                        LocationHistory history = (LocationHistory) args[0];
                        historyKeys.add(key(
                                history.getAgent().getId(),
                                history.getRecordedAt(),
                                history.getSource()
                        ));
                        yield history;
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

    private String key(String agentId, Instant recordedAt, LocationSource source) {
        return agentId + "|" + recordedAt + "|" + source.name();
    }

    private static class ExternalLocationGatewayFake implements ExternalLocationGateway {
        private List<ExternalLocationDto> locations = List.of();
        private RuntimeException exception;

        @Override
        public List<ExternalLocationDto> fetchAllLocations() {
            if (exception != null) {
                throw exception;
            }

            return locations;
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

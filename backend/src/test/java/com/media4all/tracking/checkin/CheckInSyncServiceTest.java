package com.media4all.tracking.checkin;

import com.media4all.tracking.agent.Agent;
import com.media4all.tracking.agent.AgentRepository;
import com.media4all.tracking.agent.AgentStatus;
import com.media4all.tracking.external.ExternalApiException;
import com.media4all.tracking.external.checkin.ExternalCheckInDto;
import com.media4all.tracking.external.checkin.ExternalCheckInGateway;
import com.media4all.tracking.location.LocationHistory;
import com.media4all.tracking.location.LocationHistoryRepository;
import com.media4all.tracking.location.LocationSource;
import com.media4all.tracking.sync.SyncExecution;
import com.media4all.tracking.sync.SyncExecutionRepository;
import com.media4all.tracking.sync.SyncState;
import com.media4all.tracking.sync.SyncStateRepository;
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

class CheckInSyncServiceTest {

    private final Map<String, Agent> agentsById = new HashMap<>();
    private final Map<String, CheckIn> checkInsById = new HashMap<>();
    private final Map<String, CheckIn> checkInsByExternalEventId = new HashMap<>();
    private final Set<String> locationHistoryKeys = new HashSet<>();
    private final Map<Long, SyncExecution> executionsById = new HashMap<>();
    private final Map<SyncType, SyncState> syncStatesByType = new HashMap<>();
    private final AtomicLong executionIds = new AtomicLong(1);
    private final AtomicLong syncStateIds = new AtomicLong(1);

    private ExternalCheckInGateway externalCheckInGateway;
    private CheckInSyncService service;

    @BeforeEach
    void setUp() {
        externalCheckInGateway = new ExternalCheckInGatewayFake();

        service = new CheckInSyncService(
                externalCheckInGateway,
                agentRepositoryProxy(),
                checkInRepositoryProxy(),
                locationHistoryRepositoryProxy(),
                syncStateRepositoryProxy(),
                syncExecutionRepositoryProxy(),
                new CheckInMapper(),
                new ImmediateTransactionTemplate()
        );
    }

    @Test
    void createsNewValidCheckIn() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        ((ExternalCheckInGatewayFake) externalCheckInGateway).checkIns =
                List.of(checkInDto("seed_ci_001", "seed_agent_001"));

        SyncResultResponse response = service.syncCheckIns();

        assertThat(response.created()).isEqualTo(1);
        assertThat(checkInsById).containsKey("seed_ci_001");
        assertThat(checkInsById.get("seed_ci_001").getExternalEventId()).isEqualTo("ext-seed_ci_001");
    }

    @Test
    void updatesExistingCheckInById() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        CheckIn existing = existingCheckIn("seed_ci_001", "seed_agent_001", "old notes");
        saveCheckIn(existing);
        ((ExternalCheckInGatewayFake) externalCheckInGateway).checkIns =
                List.of(checkInDto("seed_ci_001", "seed_agent_001", "new notes"));

        SyncResultResponse response = service.syncCheckIns();

        assertThat(response.updated()).isEqualTo(1);
        assertThat(response.created()).isZero();
        assertThat(checkInsById.get("seed_ci_001").getNotes()).isEqualTo("new notes");
    }

    @Test
    void secondRunDoesNotDuplicateCheckIn() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        ((ExternalCheckInGatewayFake) externalCheckInGateway).checkIns =
                List.of(checkInDto("seed_ci_001", "seed_agent_001"));

        SyncResultResponse first = service.syncCheckIns();
        SyncResultResponse second = service.syncCheckIns();

        assertThat(first.created()).isEqualTo(1);
        assertThat(second.created()).isZero();
        assertThat(second.updated()).isEqualTo(1);
        assertThat(checkInsById).hasSize(1);
    }

    @Test
    void usesExternalEventIdToDetectConflict() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        CheckIn existing = existingCheckIn("seed_ci_001", "seed_agent_001", "notes");
        existing.setExternalEventId("ext-conflict");
        saveCheckIn(existing);
        ((ExternalCheckInGatewayFake) externalCheckInGateway).checkIns =
                List.of(checkInDtoWithExternalEventId("seed_ci_999", "seed_agent_001", "ext-conflict"));

        SyncResultResponse response = service.syncCheckIns();

        assertThat(response.skipped()).isEqualTo(1);
        assertThat(checkInsById).doesNotContainKey("seed_ci_999");
        assertThat(executionsById.get(1L).getErrorMessage()).contains("externalEventId=ext-conflict");
    }

    @Test
    void skipsCheckInWhenAgentDoesNotExist() {
        ((ExternalCheckInGatewayFake) externalCheckInGateway).checkIns =
                List.of(checkInDto("seed_ci_001", "seed_agent_999"));

        SyncResultResponse response = service.syncCheckIns();

        assertThat(response.skipped()).isEqualTo(1);
        assertThat(checkInsById).isEmpty();
    }

    @Test
    void skipsCheckInWithNullId() {
        assertSkipped(invalidDto(null, "seed_agent_001", CheckInType.CHECKIN, CheckInSource.MANUAL, occurredAt()));
    }

    @Test
    void skipsCheckInWithNullAgentId() {
        assertSkipped(invalidDto("seed_ci_001", null, CheckInType.CHECKIN, CheckInSource.MANUAL, occurredAt()));
    }

    @Test
    void skipsCheckInWithNullType() {
        assertSkipped(invalidDto("seed_ci_001", "seed_agent_001", null, CheckInSource.MANUAL, occurredAt()));
    }

    @Test
    void skipsCheckInWithNullSource() {
        assertSkipped(invalidDto("seed_ci_001", "seed_agent_001", CheckInType.CHECKIN, null, occurredAt()));
    }

    @Test
    void skipsCheckInWithNullOccurredAt() {
        assertSkipped(invalidDto("seed_ci_001", "seed_agent_001", CheckInType.CHECKIN, CheckInSource.MANUAL, null));
    }

    @Test
    void savesCheckInWithPoorAccuracyButDoesNotCreateLocationHistory() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        ((ExternalCheckInGatewayFake) externalCheckInGateway).checkIns =
                List.of(checkInDto("seed_ci_001", "seed_agent_001", BigDecimal.valueOf(51)));

        SyncResultResponse response = service.syncCheckIns();

        assertThat(response.created()).isEqualTo(1);
        assertThat(checkInsById).containsKey("seed_ci_001");
        assertThat(locationHistoryKeys).isEmpty();
    }

    @Test
    void createsLocationHistoryForValidCoordinatesAndAcceptedAccuracy() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        ((ExternalCheckInGatewayFake) externalCheckInGateway).checkIns =
                List.of(checkInDto("seed_ci_001", "seed_agent_001", BigDecimal.valueOf(50)));

        service.syncCheckIns();

        assertThat(locationHistoryKeys).contains("seed_agent_001|2026-05-22T06:00:00Z|MANUAL_CHECKIN");
    }

    @Test
    void createsLocationHistoryWhenAccuracyIsNull() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        ((ExternalCheckInGatewayFake) externalCheckInGateway).checkIns =
                List.of(checkInDto("seed_ci_001", "seed_agent_001", (BigDecimal) null));

        service.syncCheckIns();

        assertThat(locationHistoryKeys).contains("seed_agent_001|2026-05-22T06:00:00Z|MANUAL_CHECKIN");
    }

    @Test
    void doesNotCreateLocationHistoryWhenCoordinatesAreMissing() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        ExternalCheckInDto dto = checkInDto("seed_ci_001", "seed_agent_001");
        ((ExternalCheckInGatewayFake) externalCheckInGateway).checkIns =
                List.of(new ExternalCheckInDto(
                        dto.id(),
                        dto.agentId(),
                        dto.type(),
                        dto.source(),
                        null,
                        dto.longitude(),
                        dto.address(),
                        dto.accuracy(),
                        dto.speed(),
                        dto.notes(),
                        dto.distanceFromPrevious(),
                        dto.externalEventId(),
                        dto.occurredAt(),
                        dto.syncedAt()
                ));

        service.syncCheckIns();

        assertThat(checkInsById).containsKey("seed_ci_001");
        assertThat(locationHistoryKeys).isEmpty();
    }

    @Test
    void doesNotDuplicateLocationHistoryWhenLogicalKeyAlreadyExists() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        locationHistoryKeys.add("seed_agent_001|2026-05-22T06:00:00Z|MANUAL_CHECKIN");
        ((ExternalCheckInGatewayFake) externalCheckInGateway).checkIns =
                List.of(checkInDto("seed_ci_001", "seed_agent_001"));

        service.syncCheckIns();

        assertThat(locationHistoryKeys).hasSize(1);
    }

    @Test
    void usesSyncStateTokenAsInitialToken() {
        SyncState syncState = new SyncState();
        syncState.setSyncType(SyncType.CHECK_INS);
        syncState.setLastSyncToken("token-123");
        syncStatesByType.put(SyncType.CHECK_INS, syncState);

        service.syncCheckIns();

        assertThat(((ExternalCheckInGatewayFake) externalCheckInGateway).receivedToken).isEqualTo("token-123");
        assertThat(executionsById.get(1L).getSyncTokenBefore()).isEqualTo("token-123");
        assertThat(executionsById.get(1L).getSyncTokenAfter()).isEqualTo("token-123");
    }

    @Test
    void updatesLastAttemptAtInSyncState() {
        service.syncCheckIns();

        assertThat(syncStatesByType.get(SyncType.CHECK_INS).getLastAttemptAt()).isNotNull();
    }

    @Test
    void updatesLastSuccessfulSyncAtOnlyOnSuccess() {
        service.syncCheckIns();

        assertThat(syncStatesByType.get(SyncType.CHECK_INS).getLastSuccessfulSyncAt()).isNotNull();

        Instant previousSuccess = syncStatesByType.get(SyncType.CHECK_INS).getLastSuccessfulSyncAt();
        ((ExternalCheckInGatewayFake) externalCheckInGateway).exception =
                new ExternalApiException("External API returned HTTP 503", 503, "unavailable");

        assertThatThrownBy(() -> service.syncCheckIns())
                .isInstanceOf(ExternalApiException.class);

        assertThat(syncStatesByType.get(SyncType.CHECK_INS).getLastSuccessfulSyncAt()).isEqualTo(previousSuccess);
        assertThat(syncStatesByType.get(SyncType.CHECK_INS).getLastAttemptAt()).isAfterOrEqualTo(previousSuccess);
    }

    @Test
    void registersSuccessfulExecution() {
        service.syncCheckIns();

        SyncExecution execution = executionsById.get(1L);
        assertThat(execution.getSyncType()).isEqualTo(SyncType.CHECK_INS);
        assertThat(execution.getStatus()).isEqualTo(SyncStatus.SUCCESS);
        assertThat(execution.getItemsProcessed()).isZero();
    }

    @Test
    void registersFailedExecutionWhenExternalClientFails() {
        ((ExternalCheckInGatewayFake) externalCheckInGateway).exception =
                new ExternalApiException("External API returned HTTP 503", 503, "unavailable");

        assertThatThrownBy(() -> service.syncCheckIns())
                .isInstanceOf(ExternalApiException.class);

        SyncExecution execution = executionsById.get(1L);
        assertThat(execution.getSyncType()).isEqualTo(SyncType.CHECK_INS);
        assertThat(execution.getStatus()).isEqualTo(SyncStatus.FAILED);
        assertThat(execution.getHttpStatus()).isEqualTo(503);
    }

    private void assertSkipped(ExternalCheckInDto dto) {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));
        ((ExternalCheckInGatewayFake) externalCheckInGateway).checkIns = List.of(dto);

        SyncResultResponse response = service.syncCheckIns();

        assertThat(response.skipped()).isEqualTo(1);
        assertThat(response.created()).isZero();
        assertThat(checkInsById).isEmpty();
    }

    private Agent existingAgent(String id) {
        Agent agent = new Agent();
        agent.setId(id);
        agent.setExternalId("ext-" + id);
        agent.setName("Ana");
        agent.setActive(true);
        agent.setStatus(AgentStatus.ONLINE);
        return agent;
    }

    private CheckIn existingCheckIn(String id, String agentId, String notes) {
        CheckIn checkIn = new CheckIn();
        checkIn.setId(id);
        checkIn.setAgent(existingAgent(agentId));
        checkIn.setType(CheckInType.CHECKIN);
        checkIn.setSource(CheckInSource.MANUAL);
        checkIn.setNotes(notes);
        checkIn.setOccurredAt(occurredAt());
        checkIn.setExternalEventId("ext-" + id);
        return checkIn;
    }

    private ExternalCheckInDto checkInDto(String id, String agentId) {
        return checkInDto(id, agentId, BigDecimal.valueOf(10));
    }

    private ExternalCheckInDto checkInDto(String id, String agentId, String notes) {
        return new ExternalCheckInDto(
                id,
                agentId,
                CheckInType.CHECKIN,
                CheckInSource.MANUAL,
                BigDecimal.valueOf(-23.5505000),
                BigDecimal.valueOf(-46.6333000),
                "Av. Paulista, 1000 - Sao Paulo, SP",
                BigDecimal.valueOf(10),
                BigDecimal.ZERO,
                notes,
                null,
                "ext-" + id,
                occurredAt(),
                Instant.parse("2026-05-23T02:35:35Z")
        );
    }

    private ExternalCheckInDto checkInDto(String id, String agentId, BigDecimal accuracy) {
        return new ExternalCheckInDto(
                id,
                agentId,
                CheckInType.CHECKIN,
                CheckInSource.MANUAL,
                BigDecimal.valueOf(-23.5505000),
                BigDecimal.valueOf(-46.6333000),
                "Av. Paulista, 1000 - Sao Paulo, SP",
                accuracy,
                BigDecimal.ZERO,
                "Inicio do turno",
                null,
                "ext-" + id,
                occurredAt(),
                Instant.parse("2026-05-23T02:35:35Z")
        );
    }

    private ExternalCheckInDto checkInDtoWithExternalEventId(String id, String agentId, String externalEventId) {
        ExternalCheckInDto dto = checkInDto(id, agentId);
        return new ExternalCheckInDto(
                dto.id(),
                dto.agentId(),
                dto.type(),
                dto.source(),
                dto.latitude(),
                dto.longitude(),
                dto.address(),
                dto.accuracy(),
                dto.speed(),
                dto.notes(),
                dto.distanceFromPrevious(),
                externalEventId,
                dto.occurredAt(),
                dto.syncedAt()
        );
    }

    private ExternalCheckInDto invalidDto(
            String id,
            String agentId,
            CheckInType type,
            CheckInSource source,
            Instant occurredAt
    ) {
        return new ExternalCheckInDto(
                id,
                agentId,
                type,
                source,
                BigDecimal.valueOf(-23.5505000),
                BigDecimal.valueOf(-46.6333000),
                "Av. Paulista, 1000 - Sao Paulo, SP",
                BigDecimal.valueOf(10),
                BigDecimal.ZERO,
                "Inicio do turno",
                null,
                id == null ? null : "ext-" + id,
                occurredAt,
                Instant.parse("2026-05-23T02:35:35Z")
        );
    }

    private Instant occurredAt() {
        return Instant.parse("2026-05-22T06:00:00Z");
    }

    private void saveCheckIn(CheckIn checkIn) {
        checkInsById.put(checkIn.getId(), checkIn);

        if (checkIn.getExternalEventId() != null) {
            checkInsByExternalEventId.put(checkIn.getExternalEventId(), checkIn);
        }
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

    private CheckInRepository checkInRepositoryProxy() {
        return (CheckInRepository) Proxy.newProxyInstance(
                CheckInRepository.class.getClassLoader(),
                new Class[]{CheckInRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.ofNullable(checkInsById.get(args[0]));
                    case "findByExternalEventId" -> Optional.ofNullable(checkInsByExternalEventId.get(args[0]));
                    case "existsByExternalEventId" -> checkInsByExternalEventId.containsKey(args[0]);
                    case "save" -> {
                        CheckIn checkIn = (CheckIn) args[0];
                        saveCheckIn(checkIn);
                        yield checkIn;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private LocationHistoryRepository locationHistoryRepositoryProxy() {
        return (LocationHistoryRepository) Proxy.newProxyInstance(
                LocationHistoryRepository.class.getClassLoader(),
                new Class[]{LocationHistoryRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "existsByAgentIdAndRecordedAtAndSource" -> locationHistoryKeys.contains(key(
                            (String) args[0],
                            (Instant) args[1],
                            (LocationSource) args[2]
                    ));
                    case "save" -> {
                        LocationHistory history = (LocationHistory) args[0];
                        locationHistoryKeys.add(key(
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

    private SyncStateRepository syncStateRepositoryProxy() {
        return (SyncStateRepository) Proxy.newProxyInstance(
                SyncStateRepository.class.getClassLoader(),
                new Class[]{SyncStateRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findBySyncType" -> Optional.ofNullable(syncStatesByType.get(args[0]));
                    case "save" -> {
                        SyncState syncState = (SyncState) args[0];
                        if (syncState.getId() == null) {
                            ReflectionTestUtils.setField(syncState, "id", syncStateIds.getAndIncrement());
                        }
                        syncStatesByType.put(syncState.getSyncType(), syncState);
                        yield syncState;
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

    private static class ExternalCheckInGatewayFake implements ExternalCheckInGateway {
        private List<ExternalCheckInDto> checkIns = List.of();
        private RuntimeException exception;
        private String receivedToken;

        @Override
        public List<ExternalCheckInDto> fetchAllCheckIns(String syncToken) {
            receivedToken = syncToken;

            if (exception != null) {
                throw exception;
            }

            return checkIns;
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

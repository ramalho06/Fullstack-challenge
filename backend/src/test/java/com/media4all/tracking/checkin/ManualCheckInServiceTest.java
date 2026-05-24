package com.media4all.tracking.checkin;

import com.media4all.tracking.agent.Agent;
import com.media4all.tracking.agent.AgentRepository;
import com.media4all.tracking.agent.AgentStatus;
import com.media4all.tracking.checkin.dto.CheckInCreateRequest;
import com.media4all.tracking.checkin.dto.CheckInResponse;
import com.media4all.tracking.common.exception.ResourceNotFoundException;
import com.media4all.tracking.location.LocationHistory;
import com.media4all.tracking.location.LocationHistoryRepository;
import com.media4all.tracking.location.LocationSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManualCheckInServiceTest {

    private final Map<String, Agent> agentsById = new HashMap<>();
    private final Map<String, CheckIn> checkInsById = new HashMap<>();
    private final Set<String> locationHistoryKeys = new HashSet<>();
    private ManualCheckInService service;

    @BeforeEach
    void setUp() {
        service = new ManualCheckInService(
                agentRepositoryProxy(),
                checkInRepositoryProxy(),
                locationHistoryRepositoryProxy(),
                new CheckInMapper()
        );
    }

    @Test
    void createsManualCheckInWithLocalIdAndManualSource() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));

        CheckInResponse response = service.createManualCheckIn(validRequest(BigDecimal.valueOf(10)));

        assertThat(response.id()).startsWith("local_ci_");
        assertThat(response.source()).isEqualTo(CheckInSource.MANUAL);
        assertThat(response.externalEventId()).isNull();
        assertThat(response.syncedAt()).isNotNull();
        assertThat(checkInsById).hasSize(1);
    }

    @Test
    void createsLocationHistoryWhenCoordinatesAreValid() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));

        service.createManualCheckIn(validRequest(BigDecimal.valueOf(10)));

        assertThat(locationHistoryKeys)
                .contains("seed_agent_001|2026-05-24T10:00:00Z|MANUAL_CHECKIN");
    }

    @Test
    void doesNotCreateLocationHistoryWhenAccuracyIsGreaterThanFifty() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));

        service.createManualCheckIn(validRequest(BigDecimal.valueOf(51)));

        assertThat(checkInsById).hasSize(1);
        assertThat(locationHistoryKeys).isEmpty();
    }

    @Test
    void doesNotCreateLocationHistoryWithoutCoordinates() {
        agentsById.put("seed_agent_001", existingAgent("seed_agent_001"));

        service.createManualCheckIn(new CheckInCreateRequest(
                "seed_agent_001",
                CheckInType.CHECKIN,
                null,
                BigDecimal.valueOf(-46.63),
                "Av. Paulista",
                BigDecimal.valueOf(10),
                BigDecimal.ZERO,
                "Manual",
                Instant.parse("2026-05-24T10:00:00Z")
        ));

        assertThat(checkInsById).hasSize(1);
        assertThat(locationHistoryKeys).isEmpty();
    }

    @Test
    void throwsNotFoundWhenAgentDoesNotExist() {
        assertThatThrownBy(() -> service.createManualCheckIn(validRequest(BigDecimal.valueOf(10))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Agent not found");
    }

    private CheckInCreateRequest validRequest(BigDecimal accuracy) {
        return new CheckInCreateRequest(
                "seed_agent_001",
                CheckInType.CHECKIN,
                BigDecimal.valueOf(-23.55),
                BigDecimal.valueOf(-46.63),
                "Av. Paulista",
                accuracy,
                BigDecimal.ZERO,
                "Manual",
                Instant.parse("2026-05-24T10:00:00Z")
        );
    }

    private Agent existingAgent(String id) {
        Agent agent = new Agent();
        agent.setId(id);
        agent.setExternalId("ext-" + id);
        agent.setName("Agent");
        agent.setActive(true);
        agent.setStatus(AgentStatus.ONLINE);
        agent.setBattery(BigDecimal.valueOf(80));
        return agent;
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
                    case "save" -> {
                        CheckIn checkIn = (CheckIn) args[0];
                        checkInsById.put(checkIn.getId(), checkIn);
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

    private String key(String agentId, Instant recordedAt, LocationSource source) {
        return agentId + "|" + recordedAt + "|" + source.name();
    }
}

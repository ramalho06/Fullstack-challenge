package com.media4all.tracking.sync;

import com.media4all.tracking.common.dto.PageResponse;
import com.media4all.tracking.config.SchedulerProperties;
import com.media4all.tracking.sync.dto.SyncExecutionResponse;
import com.media4all.tracking.sync.dto.SyncStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SyncMonitoringServiceTest {

    private SyncExecutionRepositoryFake repositoryFake;
    private SchedulerProperties schedulerProperties;
    private SyncMonitoringService service;

    @BeforeEach
    void setUp() {
        repositoryFake = new SyncExecutionRepositoryFake();
        schedulerProperties = new SchedulerProperties();
        service = new SyncMonitoringService(repositoryFake.repository(), schedulerProperties);
    }

    @Test
    void getLatestExecutionsReturnsOneExecutionPerTypeInLogicalOrder() {
        repositoryFake.latestByType.put(SyncType.LOCATIONS, execution(2L, SyncType.LOCATIONS, SyncStatus.SUCCESS));
        repositoryFake.latestByType.put(SyncType.AGENTS, execution(1L, SyncType.AGENTS, SyncStatus.SUCCESS));
        repositoryFake.latestByType.put(SyncType.GEOFENCES, execution(4L, SyncType.GEOFENCES, SyncStatus.SUCCESS));

        List<SyncExecutionResponse> response = service.getLatestExecutions();

        assertThat(response)
                .extracting(SyncExecutionResponse::syncType)
                .containsExactly(SyncType.AGENTS, SyncType.LOCATIONS, SyncType.GEOFENCES);
    }

    @Test
    void getSyncStatusReturnsHealthyWhenLatestExecutionsHaveNoFailuresOrPartialSuccess() {
        repositoryFake.latestByType.put(SyncType.AGENTS, execution(1L, SyncType.AGENTS, SyncStatus.SUCCESS));
        repositoryFake.latestByType.put(SyncType.LOCATIONS, execution(2L, SyncType.LOCATIONS, SyncStatus.SUCCESS));
        repositoryFake.latestByType.put(SyncType.CHECK_INS, execution(3L, SyncType.CHECK_INS, SyncStatus.SUCCESS));
        repositoryFake.latestByType.put(SyncType.GEOFENCES, execution(4L, SyncType.GEOFENCES, SyncStatus.SUCCESS));

        SyncStatusResponse response = service.getSyncStatus();

        assertThat(response.overallStatus()).isEqualTo(OperationalSyncStatus.HEALTHY);
    }

    @Test
    void getSyncStatusReturnsWarningWhenAnyLatestExecutionIsPartialSuccessAndNoneFailed() {
        repositoryFake.latestByType.put(SyncType.AGENTS, execution(1L, SyncType.AGENTS, SyncStatus.SUCCESS));
        repositoryFake.latestByType.put(SyncType.LOCATIONS, execution(2L, SyncType.LOCATIONS, SyncStatus.PARTIAL_SUCCESS));
        repositoryFake.latestByType.put(SyncType.CHECK_INS, execution(3L, SyncType.CHECK_INS, SyncStatus.SUCCESS));
        repositoryFake.latestByType.put(SyncType.GEOFENCES, execution(4L, SyncType.GEOFENCES, SyncStatus.SUCCESS));

        SyncStatusResponse response = service.getSyncStatus();

        assertThat(response.overallStatus()).isEqualTo(OperationalSyncStatus.WARNING);
    }

    @Test
    void getSyncStatusReturnsDegradedWhenAnyLatestExecutionFailed() {
        repositoryFake.latestByType.put(SyncType.AGENTS, execution(1L, SyncType.AGENTS, SyncStatus.SUCCESS));
        repositoryFake.latestByType.put(SyncType.LOCATIONS, execution(2L, SyncType.LOCATIONS, SyncStatus.FAILED));
        repositoryFake.latestByType.put(SyncType.CHECK_INS, execution(3L, SyncType.CHECK_INS, SyncStatus.SUCCESS));
        repositoryFake.latestByType.put(SyncType.GEOFENCES, execution(4L, SyncType.GEOFENCES, SyncStatus.SUCCESS));

        SyncStatusResponse response = service.getSyncStatus();

        assertThat(response.overallStatus()).isEqualTo(OperationalSyncStatus.DEGRADED);
    }

    @Test
    void getSyncStatusGivesFailedPriorityOverPartialSuccess() {
        repositoryFake.latestByType.put(SyncType.AGENTS, execution(1L, SyncType.AGENTS, SyncStatus.PARTIAL_SUCCESS));
        repositoryFake.latestByType.put(SyncType.LOCATIONS, execution(2L, SyncType.LOCATIONS, SyncStatus.FAILED));
        repositoryFake.latestByType.put(SyncType.CHECK_INS, execution(3L, SyncType.CHECK_INS, SyncStatus.SUCCESS));
        repositoryFake.latestByType.put(SyncType.GEOFENCES, execution(4L, SyncType.GEOFENCES, SyncStatus.SUCCESS));

        SyncStatusResponse response = service.getSyncStatus();

        assertThat(response.overallStatus()).isEqualTo(OperationalSyncStatus.DEGRADED);
    }

    @Test
    void listExecutionsAppliesDefaultSortAndPagination() {
        SyncExecution execution = execution(1L, SyncType.AGENTS, SyncStatus.FAILED);
        repositoryFake.pageContent = List.of(execution);

        PageResponse<SyncExecutionResponse> response = service.listExecutions(
                SyncType.AGENTS,
                SyncStatus.FAILED,
                PageRequest.of(0, 20)
        );

        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(repositoryFake.lastPageable.getSort())
                .isEqualTo(Sort.by(Sort.Direction.DESC, "startedAt"));
    }

    @Test
    void getSyncStatusReturnsSchedulerProperties() {
        schedulerProperties.setEnabled(true);
        schedulerProperties.setAgentsFixedDelayMs(600000);
        schedulerProperties.setAgentsInitialDelayMs(30000);
        repositoryFake.latestByType.put(SyncType.AGENTS, execution(1L, SyncType.AGENTS, SyncStatus.SUCCESS));

        SyncStatusResponse response = service.getSyncStatus();

        assertThat(response.syncs())
                .filteredOn(sync -> sync.syncType() == SyncType.AGENTS)
                .singleElement()
                .satisfies(sync -> {
                    assertThat(sync.schedulerEnabled()).isTrue();
                    assertThat(sync.fixedDelayMs()).isEqualTo(600000);
                    assertThat(sync.initialDelayMs()).isEqualTo(30000);
                });
    }

    @Test
    void mapperReturnsOnlyFirstLineOfErrorMessage() {
        SyncExecution execution = execution(1L, SyncType.AGENTS, SyncStatus.FAILED);
        execution.setErrorMessage("External API failed\n\tat internal.stack.Trace");
        repositoryFake.pageContent = List.of(execution);

        PageResponse<SyncExecutionResponse> response = service.listExecutions(
                null,
                null,
                PageRequest.of(0, 20)
        );

        assertThat(response.content().get(0).errorMessage()).isEqualTo("External API failed");
    }

    private SyncExecution execution(Long id, SyncType syncType, SyncStatus status) {
        SyncExecution execution = new SyncExecution();
        execution.setId(id);
        execution.setSyncType(syncType);
        execution.setStatus(status);
        execution.setStartedAt(Instant.parse("2026-05-24T09:00:00Z").plusSeconds(id));
        execution.setFinishedAt(Instant.parse("2026-05-24T09:00:01Z").plusSeconds(id));
        execution.setItemsProcessed(5);
        execution.setItemsCreated(1);
        execution.setItemsUpdated(4);
        execution.setItemsSkipped(0);
        return execution;
    }

    private static class SyncExecutionRepositoryFake implements InvocationHandler {

        private final Map<SyncType, SyncExecution> latestByType = new HashMap<>();
        private final Map<SyncStatus, SyncExecution> latestByStatus = new HashMap<>();
        private List<SyncExecution> pageContent = List.of();
        private Pageable lastPageable;

        private SyncExecutionRepository repository() {
            return (SyncExecutionRepository) Proxy.newProxyInstance(
                    SyncExecutionRepository.class.getClassLoader(),
                    new Class<?>[]{SyncExecutionRepository.class},
                    this
            );
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "findTopBySyncTypeOrderByStartedAtDesc" ->
                        Optional.ofNullable(latestByType.get((SyncType) args[0]));
                case "findTopByStatusOrderByFinishedAtDesc" ->
                        Optional.ofNullable(latestByStatus.get((SyncStatus) args[0]));
                case "count" -> (long) latestByType.size();
                case "countByStatus" -> latestByType.values().stream()
                        .filter(execution -> execution.getStatus() == args[0])
                        .count();
                case "findAll" -> handleFindAll(args);
                case "toString" -> "SyncExecutionRepositoryFake";
                default -> throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
            };
        }

        private Object handleFindAll(Object[] args) {
            lastPageable = (Pageable) args[1];
            return new PageImpl<>(pageContent, lastPageable, pageContent.size());
        }
    }
}

package com.media4all.tracking.sync;

import com.media4all.tracking.common.dto.PageResponse;
import com.media4all.tracking.config.SchedulerProperties;
import com.media4all.tracking.sync.dto.SchedulerInfoResponse;
import com.media4all.tracking.sync.dto.SyncExecutionResponse;
import com.media4all.tracking.sync.dto.SyncStatusResponse;
import com.media4all.tracking.sync.dto.SyncTypeStatusResponse;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class SyncMonitoringService {

    private static final List<SyncType> MONITORED_SYNC_TYPES = List.of(
            SyncType.AGENTS,
            SyncType.LOCATIONS,
            SyncType.CHECK_INS,
            SyncType.GEOFENCES,
            SyncType.FULL_SYNC
    );

    private final SyncExecutionRepository syncExecutionRepository;
    private final SchedulerProperties schedulerProperties;

    public SyncMonitoringService(
            SyncExecutionRepository syncExecutionRepository,
            SchedulerProperties schedulerProperties
    ) {
        this.syncExecutionRepository = syncExecutionRepository;
        this.schedulerProperties = schedulerProperties;
    }

    @Transactional(readOnly = true)
    public PageResponse<SyncExecutionResponse> listExecutions(
            SyncType syncType,
            SyncStatus status,
            Pageable pageable
    ) {
        Pageable pageableWithDefaultSort = applyDefaultSort(pageable);
        Page<SyncExecutionResponse> page = syncExecutionRepository
                .findAll(buildSpecification(syncType, status), pageableWithDefaultSort)
                .map(SyncExecutionResponse::from);

        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public List<SyncExecutionResponse> getLatestExecutions() {
        return MONITORED_SYNC_TYPES.stream()
                .map(syncExecutionRepository::findTopBySyncTypeOrderByStartedAtDesc)
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingInt(execution -> syncTypeOrder(execution.getSyncType())))
                .map(SyncExecutionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SyncStatusResponse getSyncStatus() {
        List<SyncTypeStatusResponse> syncs = MONITORED_SYNC_TYPES.stream()
                .map(this::buildSyncTypeStatus)
                .toList();

        return new SyncStatusResponse(
                calculateOverallStatus(syncs),
                findLastFinishedAtByStatus(SyncStatus.SUCCESS),
                findLastFinishedAtByStatus(SyncStatus.FAILED),
                syncExecutionRepository.count(),
                syncExecutionRepository.countByStatus(SyncStatus.FAILED),
                syncs
        );
    }

    private Pageable applyDefaultSort(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "startedAt")
        );
    }

    private Specification<SyncExecution> buildSpecification(SyncType syncType, SyncStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (syncType != null) {
                predicates.add(criteriaBuilder.equal(root.get("syncType"), syncType));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private SyncTypeStatusResponse buildSyncTypeStatus(SyncType syncType) {
        SchedulerInfoResponse schedulerInfo = schedulerInfo(syncType);
        SyncExecutionResponse lastExecution = syncExecutionRepository
                .findTopBySyncTypeOrderByStartedAtDesc(syncType)
                .map(SyncExecutionResponse::from)
                .orElse(null);

        return SyncTypeStatusResponse.of(schedulerInfo, lastExecution);
    }

    private SchedulerInfoResponse schedulerInfo(SyncType syncType) {
        return switch (syncType) {
            case AGENTS -> new SchedulerInfoResponse(
                    syncType,
                    schedulerProperties.isEnabled(),
                    schedulerProperties.getAgentsFixedDelayMs(),
                    schedulerProperties.getAgentsInitialDelayMs()
            );
            case LOCATIONS -> new SchedulerInfoResponse(
                    syncType,
                    schedulerProperties.isEnabled(),
                    schedulerProperties.getLocationsFixedDelayMs(),
                    schedulerProperties.getLocationsInitialDelayMs()
            );
            case CHECK_INS -> new SchedulerInfoResponse(
                    syncType,
                    schedulerProperties.isEnabled(),
                    schedulerProperties.getCheckInsFixedDelayMs(),
                    schedulerProperties.getCheckInsInitialDelayMs()
            );
            case GEOFENCES -> new SchedulerInfoResponse(
                    syncType,
                    schedulerProperties.isEnabled(),
                    schedulerProperties.getGeofencesFixedDelayMs(),
                    schedulerProperties.getGeofencesInitialDelayMs()
            );
            case FULL_SYNC -> new SchedulerInfoResponse(syncType, false, 0, 0);
        };
    }

    private OperationalSyncStatus calculateOverallStatus(List<SyncTypeStatusResponse> syncs) {
        List<SyncStatus> latestStatuses = syncs.stream()
                .map(SyncTypeStatusResponse::lastExecution)
                .filter(lastExecution -> lastExecution != null)
                .map(SyncExecutionResponse::status)
                .toList();

        if (latestStatuses.contains(SyncStatus.FAILED)) {
            return OperationalSyncStatus.DEGRADED;
        }

        if (latestStatuses.contains(SyncStatus.PARTIAL_SUCCESS)) {
            return OperationalSyncStatus.WARNING;
        }

        return OperationalSyncStatus.HEALTHY;
    }

    private Instant findLastFinishedAtByStatus(SyncStatus status) {
        return syncExecutionRepository.findTopByStatusOrderByFinishedAtDesc(status)
                .map(SyncExecution::getFinishedAt)
                .orElse(null);
    }

    private int syncTypeOrder(SyncType syncType) {
        return Arrays.asList(SyncType.values()).indexOf(syncType);
    }
}

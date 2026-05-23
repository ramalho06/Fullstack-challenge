package com.media4all.tracking.location;

import com.media4all.tracking.agent.Agent;
import com.media4all.tracking.agent.AgentRepository;
import com.media4all.tracking.external.ExternalApiException;
import com.media4all.tracking.external.location.ExternalLocationDto;
import com.media4all.tracking.external.location.ExternalLocationGateway;
import com.media4all.tracking.sync.SyncExecution;
import com.media4all.tracking.sync.SyncExecutionRepository;
import com.media4all.tracking.sync.SyncStatus;
import com.media4all.tracking.sync.SyncType;
import com.media4all.tracking.sync.dto.SyncResultResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class LocationSyncService {

    private static final BigDecimal MAX_ACCEPTED_ACCURACY = BigDecimal.valueOf(50);

    private final ExternalLocationGateway externalLocationGateway;
    private final AgentRepository agentRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final LocationMapper locationMapper;
    private final SyncExecutionRepository syncExecutionRepository;
    private final TransactionTemplate transactionTemplate;

    public LocationSyncService(
            ExternalLocationGateway externalLocationGateway,
            AgentRepository agentRepository,
            LocationHistoryRepository locationHistoryRepository,
            LocationMapper locationMapper,
            SyncExecutionRepository syncExecutionRepository,
            TransactionTemplate transactionTemplate
    ) {
        this.externalLocationGateway = externalLocationGateway;
        this.agentRepository = agentRepository;
        this.locationHistoryRepository = locationHistoryRepository;
        this.locationMapper = locationMapper;
        this.syncExecutionRepository = syncExecutionRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public SyncResultResponse syncLocations() {
        SyncExecution execution = createRunningExecution();

        try {
            List<ExternalLocationDto> externalLocations = externalLocationGateway.fetchAllLocations();
            SyncCounters counters = persistLocations(externalLocations);
            SyncExecution finishedExecution = markSuccess(execution.getId(), counters);
            return SyncResultResponse.from(finishedExecution);
        } catch (RuntimeException exception) {
            SyncExecution failedExecution = markFailed(execution.getId(), exception);

            if (exception instanceof ExternalApiException) {
                throw exception;
            }

            throw new LocationSyncException("Failed to synchronize locations", failedExecution.getId(), exception);
        }
    }

    private SyncExecution createRunningExecution() {
        return transactionTemplate.execute(status -> {
            SyncExecution execution = new SyncExecution();
            execution.setSyncType(SyncType.LOCATIONS);
            execution.setStatus(SyncStatus.RUNNING);
            execution.setStartedAt(Instant.now());
            execution.setItemsProcessed(0);
            execution.setItemsCreated(0);
            execution.setItemsUpdated(0);
            execution.setItemsSkipped(0);
            return syncExecutionRepository.save(execution);
        });
    }

    private SyncCounters persistLocations(List<ExternalLocationDto> externalLocations) {
        return transactionTemplate.execute(status -> {
            SyncCounters counters = new SyncCounters();

            for (ExternalLocationDto dto : externalLocations) {
                counters.processed++;

                if (!isValid(dto) || hasPoorAccuracy(dto)) {
                    counters.skipped++;
                    continue;
                }

                Agent agent = agentRepository.findById(dto.agentId()).orElse(null);

                if (agent == null) {
                    counters.skipped++;
                    continue;
                }

                locationMapper.updateAgentCurrentLocation(agent, dto);
                counters.updated++;

                if (!locationHistoryRepository.existsByAgentIdAndRecordedAtAndSource(
                        dto.agentId(),
                        dto.lastSeen(),
                        LocationSource.GPS_SYNC
                )) {
                    locationHistoryRepository.save(locationMapper.toHistory(agent, dto));
                    counters.created++;
                }
            }

            return counters;
        });
    }

    private SyncExecution markSuccess(Long executionId, SyncCounters counters) {
        return transactionTemplate.execute(status -> {
            SyncExecution execution = syncExecutionRepository.findById(executionId)
                    .orElseThrow(() -> new IllegalStateException("SyncExecution not found: " + executionId));

            execution.setStatus(SyncStatus.SUCCESS);
            execution.setFinishedAt(Instant.now());
            execution.setItemsProcessed(counters.processed);
            execution.setItemsCreated(counters.created);
            execution.setItemsUpdated(counters.updated);
            execution.setItemsSkipped(counters.skipped);

            return syncExecutionRepository.save(execution);
        });
    }

    private SyncExecution markFailed(Long executionId, RuntimeException exception) {
        return transactionTemplate.execute(status -> {
            SyncExecution execution = syncExecutionRepository.findById(executionId)
                    .orElseThrow(() -> new IllegalStateException("SyncExecution not found: " + executionId));

            execution.setStatus(SyncStatus.FAILED);
            execution.setFinishedAt(Instant.now());
            execution.setErrorMessage(exception.getMessage());

            if (exception instanceof ExternalApiException externalApiException) {
                execution.setHttpStatus(externalApiException.getHttpStatus());
            }

            return syncExecutionRepository.save(execution);
        });
    }

    private boolean isValid(ExternalLocationDto dto) {
        return dto != null
                && StringUtils.hasText(dto.agentId())
                && dto.latitude() != null
                && dto.longitude() != null
                && dto.lastSeen() != null;
    }

    private boolean hasPoorAccuracy(ExternalLocationDto dto) {
        return dto.accuracy() != null && dto.accuracy().compareTo(MAX_ACCEPTED_ACCURACY) > 0;
    }

    private static class SyncCounters {
        private int processed;
        private int created;
        private int updated;
        private int skipped;
    }
}

package com.media4all.tracking.checkin;

import com.media4all.tracking.agent.Agent;
import com.media4all.tracking.agent.AgentRepository;
import com.media4all.tracking.external.ExternalApiException;
import com.media4all.tracking.external.checkin.ExternalCheckInDto;
import com.media4all.tracking.external.checkin.ExternalCheckInGateway;
import com.media4all.tracking.location.LocationHistoryRepository;
import com.media4all.tracking.location.LocationSource;
import com.media4all.tracking.sync.SyncExecution;
import com.media4all.tracking.sync.SyncExecutionRepository;
import com.media4all.tracking.sync.SyncFailureMessage;
import com.media4all.tracking.sync.SyncState;
import com.media4all.tracking.sync.SyncStateRepository;
import com.media4all.tracking.sync.SyncStatus;
import com.media4all.tracking.sync.SyncType;
import com.media4all.tracking.sync.dto.SyncResultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class CheckInSyncService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInSyncService.class);
    private static final BigDecimal MAX_LOCATION_HISTORY_ACCURACY = BigDecimal.valueOf(50);

    private final ExternalCheckInGateway externalCheckInGateway;
    private final AgentRepository agentRepository;
    private final CheckInRepository checkInRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final SyncStateRepository syncStateRepository;
    private final SyncExecutionRepository syncExecutionRepository;
    private final CheckInMapper checkInMapper;
    private final TransactionTemplate transactionTemplate;

    public CheckInSyncService(
            ExternalCheckInGateway externalCheckInGateway,
            AgentRepository agentRepository,
            CheckInRepository checkInRepository,
            LocationHistoryRepository locationHistoryRepository,
            SyncStateRepository syncStateRepository,
            SyncExecutionRepository syncExecutionRepository,
            CheckInMapper checkInMapper,
            TransactionTemplate transactionTemplate
    ) {
        this.externalCheckInGateway = externalCheckInGateway;
        this.agentRepository = agentRepository;
        this.checkInRepository = checkInRepository;
        this.locationHistoryRepository = locationHistoryRepository;
        this.syncStateRepository = syncStateRepository;
        this.syncExecutionRepository = syncExecutionRepository;
        this.checkInMapper = checkInMapper;
        this.transactionTemplate = transactionTemplate;
    }

    public SyncResultResponse syncCheckIns() {
        SyncState syncState = getOrCreateSyncState();
        String syncTokenBefore = syncState.getLastSyncToken();
        SyncExecution execution = createRunningExecution(syncTokenBefore);

        try {
            List<ExternalCheckInDto> externalCheckIns = externalCheckInGateway.fetchAllCheckIns(syncTokenBefore);
            SyncCounters counters = persistCheckIns(externalCheckIns);
            SyncExecution finishedExecution = markSuccess(execution.getId(), counters, syncTokenBefore);
            updateSyncStateAfterSuccess();
            return SyncResultResponse.from(finishedExecution);
        } catch (RuntimeException exception) {
            SyncExecution failedExecution = markFailed(execution.getId(), exception);
            updateSyncStateAfterFailure();

            if (exception instanceof ExternalApiException) {
                throw exception;
            }

            throw new CheckInSyncException("Failed to synchronize check-ins", failedExecution.getId(), exception);
        }
    }

    private SyncState getOrCreateSyncState() {
        return transactionTemplate.execute(status -> syncStateRepository.findBySyncType(SyncType.CHECK_INS)
                .orElseGet(() -> {
                    SyncState syncState = new SyncState();
                    syncState.setSyncType(SyncType.CHECK_INS);
                    return syncStateRepository.save(syncState);
                }));
    }

    private SyncExecution createRunningExecution(String syncTokenBefore) {
        return transactionTemplate.execute(status -> {
            SyncExecution execution = new SyncExecution();
            execution.setSyncType(SyncType.CHECK_INS);
            execution.setStatus(SyncStatus.RUNNING);
            execution.setStartedAt(Instant.now());
            execution.setItemsProcessed(0);
            execution.setItemsCreated(0);
            execution.setItemsUpdated(0);
            execution.setItemsSkipped(0);
            execution.setSyncTokenBefore(syncTokenBefore);
            return syncExecutionRepository.save(execution);
        });
    }

    private SyncCounters persistCheckIns(List<ExternalCheckInDto> externalCheckIns) {
        return transactionTemplate.execute(status -> {
            SyncCounters counters = new SyncCounters();

            for (ExternalCheckInDto dto : externalCheckIns) {
                counters.processed++;

                if (!isValid(dto)) {
                    counters.skipped++;
                    continue;
                }

                Agent agent = agentRepository.findById(dto.agentId()).orElse(null);

                if (agent == null) {
                    counters.skipped++;
                    continue;
                }

                if (hasExternalEventConflict(dto, counters)) {
                    counters.skipped++;
                    continue;
                }

                CheckIn checkIn = checkInRepository.findById(dto.id()).orElse(null);

                if (checkIn == null) {
                    checkIn = checkInRepository.save(checkInMapper.toEntity(dto, agent));
                    counters.created++;
                } else {
                    checkInMapper.updateEntity(checkIn, dto, agent);
                    counters.updated++;
                }

                createLocationHistoryIfApplicable(checkIn);
            }

            return counters;
        });
    }

    private boolean hasExternalEventConflict(ExternalCheckInDto dto, SyncCounters counters) {
        if (!StringUtils.hasText(dto.externalEventId())) {
            return false;
        }

        CheckIn existingByExternalEventId = checkInRepository.findByExternalEventId(dto.externalEventId())
                .orElse(null);

        if (existingByExternalEventId == null || dto.id().equals(existingByExternalEventId.getId())) {
            return false;
        }

        String conflict = "externalEventId=" + dto.externalEventId()
                + " persistedId=" + existingByExternalEventId.getId()
                + " incomingId=" + dto.id();
        counters.conflicts.add(conflict);
        LOGGER.warn("Check-in sync conflict: {}", conflict);
        return true;
    }

    private void createLocationHistoryIfApplicable(CheckIn checkIn) {
        if (!shouldCreateLocationHistory(checkIn)) {
            return;
        }

        LocationSource locationSource = checkInMapper.mapToLocationSource(checkIn.getSource());
        boolean alreadyExists = locationHistoryRepository.existsByAgentIdAndRecordedAtAndSource(
                checkIn.getAgent().getId(),
                checkIn.getOccurredAt(),
                locationSource
        );

        if (!alreadyExists) {
            locationHistoryRepository.save(checkInMapper.toLocationHistory(checkIn));
        }
    }

    private boolean shouldCreateLocationHistory(CheckIn checkIn) {
        return checkIn.getLatitude() != null
                && checkIn.getLongitude() != null
                && checkIn.getOccurredAt() != null
                && (checkIn.getAccuracy() == null
                || checkIn.getAccuracy().compareTo(MAX_LOCATION_HISTORY_ACCURACY) <= 0);
    }

    private SyncExecution markSuccess(Long executionId, SyncCounters counters, String syncTokenBefore) {
        return transactionTemplate.execute(status -> {
            SyncExecution execution = syncExecutionRepository.findById(executionId)
                    .orElseThrow(() -> new IllegalStateException("SyncExecution not found: " + executionId));

            execution.setStatus(SyncStatus.SUCCESS);
            execution.setFinishedAt(Instant.now());
            execution.setItemsProcessed(counters.processed);
            execution.setItemsCreated(counters.created);
            execution.setItemsUpdated(counters.updated);
            execution.setItemsSkipped(counters.skipped);
            execution.setSyncTokenAfter(syncTokenBefore);

            if (!counters.conflicts.isEmpty()) {
                execution.setErrorMessage("Controlled conflicts: " + String.join("; ", counters.conflicts));
            }

            return syncExecutionRepository.save(execution);
        });
    }

    private SyncExecution markFailed(Long executionId, RuntimeException exception) {
        return transactionTemplate.execute(status -> {
            SyncExecution execution = syncExecutionRepository.findById(executionId)
                    .orElseThrow(() -> new IllegalStateException("SyncExecution not found: " + executionId));

            execution.setStatus(SyncStatus.FAILED);
            execution.setFinishedAt(Instant.now());
            execution.setErrorMessage(SyncFailureMessage.resolve(exception, SyncType.CHECK_INS));

            if (exception instanceof ExternalApiException externalApiException) {
                execution.setHttpStatus(externalApiException.getHttpStatus());
            }

            return syncExecutionRepository.save(execution);
        });
    }

    private void updateSyncStateAfterSuccess() {
        transactionTemplate.executeWithoutResult(status -> {
            SyncState syncState = syncStateRepository.findBySyncType(SyncType.CHECK_INS)
                    .orElseThrow(() -> new IllegalStateException("SyncState not found for CHECK_INS"));
            Instant now = Instant.now();
            syncState.setLastAttemptAt(now);
            syncState.setLastSuccessfulSyncAt(now);
            syncStateRepository.save(syncState);
        });
    }

    private void updateSyncStateAfterFailure() {
        transactionTemplate.executeWithoutResult(status -> {
            SyncState syncState = syncStateRepository.findBySyncType(SyncType.CHECK_INS)
                    .orElse(null);

            if (syncState != null) {
                syncState.setLastAttemptAt(Instant.now());
                syncStateRepository.save(syncState);
            }
        });
    }

    private boolean isValid(ExternalCheckInDto dto) {
        return dto != null
                && StringUtils.hasText(dto.id())
                && StringUtils.hasText(dto.agentId())
                && dto.type() != null
                && dto.source() != null
                && dto.occurredAt() != null;
    }

    private static class SyncCounters {
        private int processed;
        private int created;
        private int updated;
        private int skipped;
        private final List<String> conflicts = new ArrayList<>();
    }
}

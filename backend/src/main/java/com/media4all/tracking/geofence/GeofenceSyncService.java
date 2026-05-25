package com.media4all.tracking.geofence;

import com.media4all.tracking.external.ExternalApiException;
import com.media4all.tracking.external.geofence.ExternalGeofenceDto;
import com.media4all.tracking.external.geofence.ExternalGeofenceGateway;
import com.media4all.tracking.sync.SyncExecution;
import com.media4all.tracking.sync.SyncExecutionRepository;
import com.media4all.tracking.sync.SyncFailureMessage;
import com.media4all.tracking.sync.SyncStatus;
import com.media4all.tracking.sync.SyncType;
import com.media4all.tracking.sync.dto.SyncResultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeofenceSyncService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeofenceSyncService.class);

    private final ExternalGeofenceGateway externalGeofenceGateway;
    private final GeofenceRepository geofenceRepository;
    private final GeofenceMapper geofenceMapper;
    private final SyncExecutionRepository syncExecutionRepository;
    private final TransactionTemplate transactionTemplate;

    public GeofenceSyncService(
            ExternalGeofenceGateway externalGeofenceGateway,
            GeofenceRepository geofenceRepository,
            GeofenceMapper geofenceMapper,
            SyncExecutionRepository syncExecutionRepository,
            TransactionTemplate transactionTemplate
    ) {
        this.externalGeofenceGateway = externalGeofenceGateway;
        this.geofenceRepository = geofenceRepository;
        this.geofenceMapper = geofenceMapper;
        this.syncExecutionRepository = syncExecutionRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public SyncResultResponse syncGeofences() {
        SyncExecution execution = createRunningExecution();

        try {
            List<ExternalGeofenceDto> externalGeofences = externalGeofenceGateway.fetchAllGeofences();
            SyncCounters counters = persistGeofences(externalGeofences);
            SyncExecution finishedExecution = markSuccess(execution.getId(), counters);
            return SyncResultResponse.from(finishedExecution);
        } catch (RuntimeException exception) {
            SyncExecution failedExecution = markFailed(execution.getId(), exception);

            if (exception instanceof ExternalApiException) {
                throw exception;
            }

            throw new GeofenceSyncException("Failed to synchronize geofences", failedExecution.getId(), exception);
        }
    }

    private SyncExecution createRunningExecution() {
        return transactionTemplate.execute(status -> {
            SyncExecution execution = new SyncExecution();
            execution.setSyncType(SyncType.GEOFENCES);
            execution.setStatus(SyncStatus.RUNNING);
            execution.setStartedAt(Instant.now());
            execution.setItemsProcessed(0);
            execution.setItemsCreated(0);
            execution.setItemsUpdated(0);
            execution.setItemsSkipped(0);
            return syncExecutionRepository.save(execution);
        });
    }

    private SyncCounters persistGeofences(List<ExternalGeofenceDto> externalGeofences) {
        return transactionTemplate.execute(status -> {
            SyncCounters counters = new SyncCounters();

            for (ExternalGeofenceDto dto : externalGeofences) {
                counters.processed++;

                if (!isValid(dto)) {
                    counters.skipped++;
                    continue;
                }

                Geofence geofence = geofenceRepository.findByExternalId(dto.externalId()).orElse(null);

                if (geofence == null) {
                    geofenceRepository.save(geofenceMapper.createFromExternal(dto));
                    counters.created++;
                    continue;
                }

                if (!geofence.getId().equals(dto.id())) {
                    registerConflict(dto, geofence, counters);
                    counters.skipped++;
                    continue;
                }

                geofenceMapper.updateFromExternal(geofence, dto);
                counters.updated++;
            }

            return counters;
        });
    }

    private void registerConflict(ExternalGeofenceDto dto, Geofence geofence, SyncCounters counters) {
        String conflict = "externalId=" + dto.externalId()
                + " persistedId=" + geofence.getId()
                + " incomingId=" + dto.id();
        counters.conflicts.add(conflict);
        LOGGER.warn("Geofence sync conflict: {}", conflict);
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
            execution.setErrorMessage(SyncFailureMessage.resolve(exception, SyncType.GEOFENCES));

            if (exception instanceof ExternalApiException externalApiException) {
                execution.setHttpStatus(externalApiException.getHttpStatus());
            }

            return syncExecutionRepository.save(execution);
        });
    }

    private boolean isValid(ExternalGeofenceDto dto) {
        return dto != null
                && StringUtils.hasText(dto.id())
                && StringUtils.hasText(dto.externalId())
                && StringUtils.hasText(dto.name())
                && dto.type() != null
                && StringUtils.hasText(dto.coordinatesJson())
                && dto.alertOnEnter() != null
                && dto.alertOnExit() != null
                && dto.syncedAt() != null;
    }

    private static class SyncCounters {
        private int processed;
        private int created;
        private int updated;
        private int skipped;
        private final List<String> conflicts = new ArrayList<>();
    }
}

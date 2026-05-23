package com.media4all.tracking.geofence;

import com.media4all.tracking.external.ExternalApiException;
import com.media4all.tracking.external.geofence.ExternalGeofenceDto;
import com.media4all.tracking.external.geofence.ExternalGeofenceGateway;
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
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeofenceSyncServiceTest {

    private final Map<String, Geofence> geofencesByExternalId = new HashMap<>();
    private final Map<Long, SyncExecution> executionsById = new HashMap<>();
    private final AtomicLong executionIds = new AtomicLong(1);

    private ExternalGeofenceGateway externalGeofenceGateway;
    private GeofenceSyncService service;

    @BeforeEach
    void setUp() {
        externalGeofenceGateway = new ExternalGeofenceGatewayFake();

        service = new GeofenceSyncService(
                externalGeofenceGateway,
                geofenceRepositoryProxy(),
                new GeofenceMapper(),
                syncExecutionRepositoryProxy(),
                new ImmediateTransactionTemplate()
        );
    }

    @Test
    void createsNewGeofence() {
        ((ExternalGeofenceGatewayFake) externalGeofenceGateway).geofences =
                List.of(geofenceDto("seed_geo_001", "ext-geo-001"));

        SyncResultResponse response = service.syncGeofences();

        assertThat(response.created()).isEqualTo(1);
        assertThat(geofencesByExternalId).hasSize(1);
        assertThat(geofencesByExternalId.get("ext-geo-001").getId()).isEqualTo("seed_geo_001");
    }

    @Test
    void updatesExistingGeofenceByExternalId() {
        Geofence existing = existingGeofence("seed_geo_001", "ext-geo-001", "Old");
        geofencesByExternalId.put(existing.getExternalId(), existing);
        ((ExternalGeofenceGatewayFake) externalGeofenceGateway).geofences =
                List.of(geofenceDto("seed_geo_001", "ext-geo-001", "New"));

        SyncResultResponse response = service.syncGeofences();

        assertThat(response.updated()).isEqualTo(1);
        assertThat(response.created()).isZero();
        assertThat(geofencesByExternalId.get("ext-geo-001").getName()).isEqualTo("New");
    }

    @Test
    void secondRunDoesNotDuplicateGeofences() {
        ((ExternalGeofenceGatewayFake) externalGeofenceGateway).geofences =
                List.of(geofenceDto("seed_geo_001", "ext-geo-001"));

        SyncResultResponse first = service.syncGeofences();
        SyncResultResponse second = service.syncGeofences();

        assertThat(first.created()).isEqualTo(1);
        assertThat(second.created()).isZero();
        assertThat(second.updated()).isEqualTo(1);
        assertThat(geofencesByExternalId).hasSize(1);
    }

    @Test
    void detectsConflictWhenExternalIdPointsToDifferentId() {
        Geofence existing = existingGeofence("seed_geo_001", "ext-geo-001", "Zona Central");
        geofencesByExternalId.put(existing.getExternalId(), existing);
        ((ExternalGeofenceGatewayFake) externalGeofenceGateway).geofences =
                List.of(geofenceDto("seed_geo_999", "ext-geo-001"));

        SyncResultResponse response = service.syncGeofences();

        assertThat(response.skipped()).isEqualTo(1);
        assertThat(response.updated()).isZero();
        assertThat(geofencesByExternalId.get("ext-geo-001").getId()).isEqualTo("seed_geo_001");
        assertThat(executionsById.get(1L).getErrorMessage()).contains("externalId=ext-geo-001");
    }

    @Test
    void skipsDtoWithNullId() {
        assertSkipped(invalidDto(null, "ext-geo-001", "Zona", GeofenceType.POLYGON, "[]", true, true, syncedAt()));
    }

    @Test
    void skipsDtoWithNullExternalId() {
        assertSkipped(invalidDto("seed_geo_001", null, "Zona", GeofenceType.POLYGON, "[]", true, true, syncedAt()));
    }

    @Test
    void skipsDtoWithNullName() {
        assertSkipped(invalidDto("seed_geo_001", "ext-geo-001", null, GeofenceType.POLYGON, "[]", true, true, syncedAt()));
    }

    @Test
    void skipsDtoWithNullType() {
        assertSkipped(invalidDto("seed_geo_001", "ext-geo-001", "Zona", null, "[]", true, true, syncedAt()));
    }

    @Test
    void skipsDtoWithNullCoordinatesJson() {
        assertSkipped(invalidDto("seed_geo_001", "ext-geo-001", "Zona", GeofenceType.POLYGON, null, true, true, syncedAt()));
    }

    @Test
    void skipsDtoWithNullAlertOnEnter() {
        assertSkipped(invalidDto("seed_geo_001", "ext-geo-001", "Zona", GeofenceType.POLYGON, "[]", null, true, syncedAt()));
    }

    @Test
    void skipsDtoWithNullAlertOnExit() {
        assertSkipped(invalidDto("seed_geo_001", "ext-geo-001", "Zona", GeofenceType.POLYGON, "[]", true, null, syncedAt()));
    }

    @Test
    void skipsDtoWithNullSyncedAt() {
        assertSkipped(invalidDto("seed_geo_001", "ext-geo-001", "Zona", GeofenceType.POLYGON, "[]", true, true, null));
    }

    @Test
    void registersSuccessfulExecution() {
        ((ExternalGeofenceGatewayFake) externalGeofenceGateway).geofences =
                List.of(geofenceDto("seed_geo_001", "ext-geo-001"));

        service.syncGeofences();

        SyncExecution execution = executionsById.get(1L);
        assertThat(execution.getSyncType()).isEqualTo(SyncType.GEOFENCES);
        assertThat(execution.getStatus()).isEqualTo(SyncStatus.SUCCESS);
        assertThat(execution.getItemsProcessed()).isEqualTo(1);
        assertThat(execution.getItemsCreated()).isEqualTo(1);
    }

    @Test
    void registersFailedExecutionWhenExternalClientFails() {
        ((ExternalGeofenceGatewayFake) externalGeofenceGateway).exception =
                new ExternalApiException("External API returned HTTP 503", 503, "unavailable");

        assertThatThrownBy(() -> service.syncGeofences())
                .isInstanceOf(ExternalApiException.class);

        SyncExecution execution = executionsById.get(1L);
        assertThat(execution.getSyncType()).isEqualTo(SyncType.GEOFENCES);
        assertThat(execution.getStatus()).isEqualTo(SyncStatus.FAILED);
        assertThat(execution.getHttpStatus()).isEqualTo(503);
    }

    private void assertSkipped(ExternalGeofenceDto dto) {
        ((ExternalGeofenceGatewayFake) externalGeofenceGateway).geofences = List.of(dto);

        SyncResultResponse response = service.syncGeofences();

        assertThat(response.skipped()).isEqualTo(1);
        assertThat(response.created()).isZero();
        assertThat(response.updated()).isZero();
        assertThat(geofencesByExternalId).isEmpty();
    }

    private Geofence existingGeofence(String id, String externalId, String name) {
        Geofence geofence = new Geofence();
        geofence.setId(id);
        geofence.setExternalId(externalId);
        geofence.setName(name);
        geofence.setType(GeofenceType.POLYGON);
        geofence.setCoordinatesJson("[]");
        geofence.setAlertOnEnter(true);
        geofence.setAlertOnExit(true);
        geofence.setSyncedAt(syncedAt());
        return geofence;
    }

    private ExternalGeofenceDto geofenceDto(String id, String externalId) {
        return geofenceDto(id, externalId, "Zona Central");
    }

    private ExternalGeofenceDto geofenceDto(String id, String externalId, String name) {
        return new ExternalGeofenceDto(
                id,
                externalId,
                name,
                GeofenceType.POLYGON,
                "[[-46.65,-23.55],[-46.6,-23.55]]",
                true,
                false,
                "Alpha,Beta",
                syncedAt()
        );
    }

    private ExternalGeofenceDto invalidDto(
            String id,
            String externalId,
            String name,
            GeofenceType type,
            String coordinatesJson,
            Boolean alertOnEnter,
            Boolean alertOnExit,
            Instant syncedAt
    ) {
        return new ExternalGeofenceDto(
                id,
                externalId,
                name,
                type,
                coordinatesJson,
                alertOnEnter,
                alertOnExit,
                "Alpha",
                syncedAt
        );
    }

    private Instant syncedAt() {
        return Instant.parse("2026-05-23T02:35:40Z");
    }

    private GeofenceRepository geofenceRepositoryProxy() {
        return (GeofenceRepository) Proxy.newProxyInstance(
                GeofenceRepository.class.getClassLoader(),
                new Class[]{GeofenceRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByExternalId" -> Optional.ofNullable(geofencesByExternalId.get(args[0]));
                    case "existsByExternalId" -> geofencesByExternalId.containsKey(args[0]);
                    case "save" -> {
                        Geofence geofence = (Geofence) args[0];
                        geofencesByExternalId.put(geofence.getExternalId(), geofence);
                        yield geofence;
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

    private static class ExternalGeofenceGatewayFake implements ExternalGeofenceGateway {
        private List<ExternalGeofenceDto> geofences = List.of();
        private RuntimeException exception;

        @Override
        public List<ExternalGeofenceDto> fetchAllGeofences() {
            if (exception != null) {
                throw exception;
            }

            return geofences;
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

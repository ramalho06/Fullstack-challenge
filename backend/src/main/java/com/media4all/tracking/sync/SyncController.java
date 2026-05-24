package com.media4all.tracking.sync;

import com.media4all.tracking.agent.AgentSyncService;
import com.media4all.tracking.checkin.CheckInSyncService;
import com.media4all.tracking.geofence.GeofenceSyncService;
import com.media4all.tracking.location.LocationSyncService;
import com.media4all.tracking.sync.dto.SyncResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync")
@Tag(name = "Sync commands")
public class SyncController {

    private final AgentSyncService agentSyncService;
    private final LocationSyncService locationSyncService;
    private final CheckInSyncService checkInSyncService;
    private final GeofenceSyncService geofenceSyncService;

    public SyncController(
            AgentSyncService agentSyncService,
            LocationSyncService locationSyncService,
            CheckInSyncService checkInSyncService,
            GeofenceSyncService geofenceSyncService
    ) {
        this.agentSyncService = agentSyncService;
        this.locationSyncService = locationSyncService;
        this.checkInSyncService = checkInSyncService;
        this.geofenceSyncService = geofenceSyncService;
    }

    @Operation(summary = "Sincronizar agentes manualmente", description = "Busca agentes da API externa e faz upsert por `externalId`.")
    @PostMapping("/agents")
    public ResponseEntity<SyncResultResponse> syncAgents() {
        return ResponseEntity.ok(agentSyncService.syncAgents());
    }

    @Operation(summary = "Sincronizar localizações manualmente", description = "Atualiza localização atual dos agentes e cria histórico de pontos válidos.")
    @PostMapping("/locations")
    public ResponseEntity<SyncResultResponse> syncLocations() {
        return ResponseEntity.ok(locationSyncService.syncLocations());
    }

    @Operation(summary = "Sincronizar check-ins manualmente", description = "Busca check-ins externos, usa idempotência por ID/evento externo e atualiza SyncState.")
    @PostMapping("/check-ins")
    public ResponseEntity<SyncResultResponse> syncCheckIns() {
        return ResponseEntity.ok(checkInSyncService.syncCheckIns());
    }

    @Operation(summary = "Sincronizar geofences manualmente", description = "Busca geofences externas e faz upsert por `externalId`.")
    @PostMapping("/geofences")
    public ResponseEntity<SyncResultResponse> syncGeofences() {
        return ResponseEntity.ok(geofenceSyncService.syncGeofences());
    }
}

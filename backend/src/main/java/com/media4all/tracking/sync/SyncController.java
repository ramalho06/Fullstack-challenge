package com.media4all.tracking.sync;

import com.media4all.tracking.agent.AgentSyncService;
import com.media4all.tracking.checkin.CheckInSyncService;
import com.media4all.tracking.location.LocationSyncService;
import com.media4all.tracking.sync.dto.SyncResultResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {

    private final AgentSyncService agentSyncService;
    private final LocationSyncService locationSyncService;
    private final CheckInSyncService checkInSyncService;

    public SyncController(
            AgentSyncService agentSyncService,
            LocationSyncService locationSyncService,
            CheckInSyncService checkInSyncService
    ) {
        this.agentSyncService = agentSyncService;
        this.locationSyncService = locationSyncService;
        this.checkInSyncService = checkInSyncService;
    }

    @PostMapping("/agents")
    public ResponseEntity<SyncResultResponse> syncAgents() {
        return ResponseEntity.ok(agentSyncService.syncAgents());
    }

    @PostMapping("/locations")
    public ResponseEntity<SyncResultResponse> syncLocations() {
        return ResponseEntity.ok(locationSyncService.syncLocations());
    }

    @PostMapping("/check-ins")
    public ResponseEntity<SyncResultResponse> syncCheckIns() {
        return ResponseEntity.ok(checkInSyncService.syncCheckIns());
    }
}

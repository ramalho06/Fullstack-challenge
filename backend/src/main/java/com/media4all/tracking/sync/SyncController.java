package com.media4all.tracking.sync;

import com.media4all.tracking.agent.AgentSyncService;
import com.media4all.tracking.sync.dto.SyncResultResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {

    private final AgentSyncService agentSyncService;

    public SyncController(AgentSyncService agentSyncService) {
        this.agentSyncService = agentSyncService;
    }

    @PostMapping("/agents")
    public ResponseEntity<SyncResultResponse> syncAgents() {
        return ResponseEntity.ok(agentSyncService.syncAgents());
    }
}

package com.media4all.tracking.sync;

import com.media4all.tracking.common.dto.PageResponse;
import com.media4all.tracking.sync.dto.SyncExecutionResponse;
import com.media4all.tracking.sync.dto.SyncStatusResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sync")
public class SyncMonitoringController {

    private final SyncMonitoringService syncMonitoringService;

    public SyncMonitoringController(SyncMonitoringService syncMonitoringService) {
        this.syncMonitoringService = syncMonitoringService;
    }

    @GetMapping("/executions")
    public PageResponse<SyncExecutionResponse> listExecutions(
            @RequestParam(required = false) SyncType syncType,
            @RequestParam(required = false) SyncStatus status,
            Pageable pageable
    ) {
        return syncMonitoringService.listExecutions(syncType, status, pageable);
    }

    @GetMapping("/executions/latest")
    public List<SyncExecutionResponse> getLatestExecutions() {
        return syncMonitoringService.getLatestExecutions();
    }

    @GetMapping("/status")
    public SyncStatusResponse getSyncStatus() {
        return syncMonitoringService.getSyncStatus();
    }
}

package com.media4all.tracking.sync;

import com.media4all.tracking.common.dto.PageResponse;
import com.media4all.tracking.sync.dto.SyncExecutionResponse;
import com.media4all.tracking.sync.dto.SyncStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sync")
@Tag(name = "Sync monitoring")
public class SyncMonitoringController {

    private final SyncMonitoringService syncMonitoringService;

    public SyncMonitoringController(SyncMonitoringService syncMonitoringService) {
        this.syncMonitoringService = syncMonitoringService;
    }

    @Operation(summary = "Listar execuções de sincronização", description = "Lista o histórico de sincronizações com filtros por tipo, status e paginação.")
    @GetMapping("/executions")
    public PageResponse<SyncExecutionResponse> listExecutions(
            @RequestParam(required = false) SyncType syncType,
            @RequestParam(required = false) SyncStatus status,
            @ParameterObject Pageable pageable
    ) {
        return syncMonitoringService.listExecutions(syncType, status, pageable);
    }

    @Operation(summary = "Últimas execuções por tipo", description = "Retorna no máximo uma última execução registrada para cada `SyncType`.")
    @GetMapping("/executions/latest")
    public List<SyncExecutionResponse> getLatestExecutions() {
        return syncMonitoringService.getLatestExecutions();
    }

    @Operation(
            summary = "Status operacional das sincronizações",
            description = "Consolida o status geral das sincronizações e expõe configuração dos schedulers.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Status operacional",
                    content = @Content(
                            schema = @Schema(implementation = SyncStatusResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "overallStatus": "HEALTHY",
                                      "lastSuccessfulSyncAt": "string",
                                      "lastFailedSyncAt": null,
                                      "totalExecutions": 0,
                                      "totalFailures": 0,
                                      "syncs": [
                                        {
                                          "syncType": "LOCATIONS",
                                          "schedulerEnabled": true,
                                          "fixedDelayMs": 0,
                                          "initialDelayMs": 0,
                                          "lastExecution": {
                                            "syncType": "LOCATIONS",
                                            "status": "SUCCESS",
                                            "itemsProcessed": 0
                                          }
                                        }
                                      ]
                                    }
                                    """)
                    )
            )
    )
    @GetMapping("/status")
    public SyncStatusResponse getSyncStatus() {
        return syncMonitoringService.getSyncStatus();
    }
}

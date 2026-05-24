package com.media4all.tracking.checkin;

import com.media4all.tracking.checkin.dto.CheckInCreateRequest;
import com.media4all.tracking.checkin.dto.CheckInResponse;
import com.media4all.tracking.common.dto.PageResponse;
import com.media4all.tracking.common.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/check-ins")
@Tag(name = "Check-ins")
public class CheckInController {

    private final CheckInQueryService checkInQueryService;
    private final ManualCheckInService manualCheckInService;

    public CheckInController(CheckInQueryService checkInQueryService, ManualCheckInService manualCheckInService) {
        this.checkInQueryService = checkInQueryService;
        this.manualCheckInService = manualCheckInService;
    }

    @Operation(summary = "Listar check-ins", description = "Lista check-ins com filtros opcionais por agente, tipo e origem.")
    @GetMapping
    public PageResponse<CheckInResponse> listCheckIns(
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) CheckInType type,
            @RequestParam(required = false) CheckInSource source,
            @ParameterObject Pageable pageable
    ) {
        return PageResponse.from(checkInQueryService.findCheckIns(agentId, type, source, pageable));
    }

    @Operation(
            summary = "Registrar check-in manual",
            description = """
                    Registra um check-in manual com `source=MANUAL` e ID `local_ci_<uuid>`.
                    Se houver coordenadas válidas e `accuracy <= 50` ou `accuracy = null`, também cria `LocationHistory`.
                    Se `accuracy > 50`, salva o CheckIn, mas não cria ponto de histórico.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = CheckInCreateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "agentId": "seed_agent_001",
                                      "type": "CHECKIN",
                                      "latitude": -23.5505,
                                      "longitude": -46.6333,
                                      "address": "Av. Paulista, 1000 - São Paulo, SP",
                                      "accuracy": 10,
                                      "speed": 0,
                                      "notes": "Check-in manual"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Check-in manual criado"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Agente não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Erro de validação",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<CheckInResponse> createManualCheckIn(@Valid @RequestBody CheckInCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(manualCheckInService.createManualCheckIn(request));
    }
}

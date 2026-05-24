package com.media4all.tracking.agent;

import com.media4all.tracking.agent.dto.AgentCreateRequest;
import com.media4all.tracking.agent.dto.AgentResponse;
import com.media4all.tracking.agent.dto.AgentUpdateRequest;
import com.media4all.tracking.common.dto.PageResponse;
import com.media4all.tracking.common.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agents")
@Tag(name = "Agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @Operation(summary = "Listar agentes", description = "Lista agentes com filtros opcionais e paginação.")
    @GetMapping
    public PageResponse<AgentResponse> listAgents(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) AgentStatus status,
            @RequestParam(required = false) AgentRole role,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable
    ) {
        return PageResponse.from(agentService.findAgents(active, status, role, team, search, pageable));
    }

    @Operation(summary = "Buscar agente por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agente encontrado"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Agente não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public AgentResponse getAgent(@Parameter(description = "ID textual do agente") @PathVariable String id) {
        return agentService.findById(id);
    }

    @Operation(
            summary = "Criar agente local",
            description = """
                    Cria um agente manual/local. O backend gera `id` no formato `local_agent_<uuid>` e
                    `externalId` no formato `local-ext-agent_<uuid>`. Campos de localização, bateria e
                    timestamps externos não são enviados no cadastro manual.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = AgentCreateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Agente Local",
                                      "role": "TECHNICIAN",
                                      "team": "Alpha",
                                      "phone": "+5511999999999",
                                      "email": "agente.local@example.com",
                                      "active": true,
                                      "status": "OFFLINE"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agente criado"),
            @ApiResponse(
                    responseCode = "422",
                    description = "Erro de validação",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<AgentResponse> createAgent(@Valid @RequestBody AgentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agentService.create(request));
    }

    @Operation(
            summary = "Atualizar agente",
            description = """
                    Atualiza apenas campos cadastrais/editáveis. Localização, bateria, `externalId` e timestamps
                    externos são controlados pelas sincronizações.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = AgentUpdateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Agente Atualizado",
                                      "role": "INSTALLER",
                                      "team": "Beta",
                                      "phone": "+5511888888888",
                                      "email": "agente.atualizado@example.com",
                                      "active": true,
                                      "status": "ONLINE"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agente atualizado"),
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
    @PutMapping("/{id}")
    public AgentResponse updateAgent(
            @Parameter(description = "ID textual do agente") @PathVariable String id,
            @Valid @RequestBody AgentUpdateRequest request
    ) {
        return agentService.update(id, request);
    }

    @Operation(summary = "Desativar agente", description = "Executa soft delete: define `active=false` e `status=OFFLINE`.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Agente desativado"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Agente não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgent(@Parameter(description = "ID textual do agente") @PathVariable String id) {
        agentService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}

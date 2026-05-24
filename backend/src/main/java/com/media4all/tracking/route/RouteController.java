package com.media4all.tracking.route;

import com.media4all.tracking.common.exception.ApiErrorResponse;
import com.media4all.tracking.route.dto.RouteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/agents")
@Tag(name = "Routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @Operation(
            summary = "Consultar rota diária do agente",
            description = """
                    Retorna o histórico de rota do dia a partir de `LocationHistory`.
                    O parâmetro `date` é interpretado no timezone `America/Sao_Paulo`.
                    Pontos com `accuracy > 50` são ignorados defensivamente e a distância usa Haversine.
                    """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Rota diária",
                    content = @Content(
                            schema = @Schema(implementation = RouteResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "agentId": "string",
                                      "date": "string",
                                      "totalDistanceMeters": 0,
                                      "points": [
                                        {
                                          "latitude": 0,
                                          "longitude": 0,
                                          "address": "string",
                                          "accuracy": 0,
                                          "speed": 0,
                                          "timestamp": "string",
                                          "source": "GPS_SYNC",
                                          "distanceFromPreviousMeters": 0
                                        }
                                      ]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Agente não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/{id}/route")
    public RouteResponse getDailyRoute(
            @Parameter(description = "ID textual do agente") @PathVariable String id,
            @Parameter(description = "Data operacional no formato YYYY-MM-DD", example = "2026-05-22")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return routeService.getDailyRoute(id, date);
    }
}

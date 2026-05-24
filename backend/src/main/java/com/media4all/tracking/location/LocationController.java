package com.media4all.tracking.location;

import com.media4all.tracking.location.dto.CurrentLocationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Locations")
public class LocationController {

    private final LocationQueryService locationQueryService;

    public LocationController(LocationQueryService locationQueryService) {
        this.locationQueryService = locationQueryService;
    }

    @Operation(summary = "Listar localizações atuais", description = "Retorna a localização atual dos agentes, com filtros opcionais.")
    @GetMapping("/api/v1/locations")
    public List<CurrentLocationResponse> listCurrentLocations(
            @RequestParam(required = false) Boolean onlineOnly,
            @RequestParam(required = false) Boolean active
    ) {
        return locationQueryService.findCurrentLocations(onlineOnly, active);
    }

    @Operation(summary = "Localização atual de um agente", description = "Retorna dados do agente e sua última localização conhecida, quando existir.")
    @GetMapping("/api/v1/agents/{id}/location")
    public CurrentLocationResponse getCurrentLocation(@PathVariable String id) {
        return locationQueryService.findCurrentLocationByAgentId(id);
    }
}

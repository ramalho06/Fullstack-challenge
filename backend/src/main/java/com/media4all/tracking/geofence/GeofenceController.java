package com.media4all.tracking.geofence;

import com.media4all.tracking.common.dto.PageResponse;
import com.media4all.tracking.geofence.dto.GeofenceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/geofences")
@Tag(name = "Geofences")
public class GeofenceController {

    private final GeofenceQueryService geofenceQueryService;

    public GeofenceController(GeofenceQueryService geofenceQueryService) {
        this.geofenceQueryService = geofenceQueryService;
    }

    @Operation(summary = "Listar geofences", description = "Lista cercas geográficas sincronizadas, com filtro opcional por tipo.")
    @GetMapping
    public PageResponse<GeofenceResponse> listGeofences(
            @RequestParam(required = false) GeofenceType type,
            @ParameterObject Pageable pageable
    ) {
        return PageResponse.from(geofenceQueryService.findGeofences(type, pageable));
    }
}

package com.media4all.tracking.location;

import com.media4all.tracking.location.dto.CurrentLocationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LocationController {

    private final LocationQueryService locationQueryService;

    public LocationController(LocationQueryService locationQueryService) {
        this.locationQueryService = locationQueryService;
    }

    @GetMapping("/api/v1/locations")
    public List<CurrentLocationResponse> listCurrentLocations(
            @RequestParam(required = false) Boolean onlineOnly,
            @RequestParam(required = false) Boolean active
    ) {
        return locationQueryService.findCurrentLocations(onlineOnly, active);
    }

    @GetMapping("/api/v1/agents/{id}/location")
    public CurrentLocationResponse getCurrentLocation(@PathVariable String id) {
        return locationQueryService.findCurrentLocationByAgentId(id);
    }
}

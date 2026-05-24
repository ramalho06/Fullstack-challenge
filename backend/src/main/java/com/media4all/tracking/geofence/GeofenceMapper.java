package com.media4all.tracking.geofence;

import com.media4all.tracking.external.geofence.ExternalGeofenceDto;
import com.media4all.tracking.geofence.dto.GeofenceResponse;
import org.springframework.stereotype.Component;

@Component
public class GeofenceMapper {

    public Geofence createFromExternal(ExternalGeofenceDto dto) {
        Geofence geofence = new Geofence();
        geofence.setId(dto.id());
        updateFromExternal(geofence, dto);
        return geofence;
    }

    public void updateFromExternal(Geofence geofence, ExternalGeofenceDto dto) {
        geofence.setExternalId(dto.externalId());
        geofence.setName(dto.name());
        geofence.setType(dto.type());
        geofence.setCoordinatesJson(dto.coordinatesJson());
        geofence.setAlertOnEnter(dto.alertOnEnter());
        geofence.setAlertOnExit(dto.alertOnExit());
        geofence.setAssignedTeams(dto.assignedTeams());
        geofence.setSyncedAt(dto.syncedAt());
    }

    public GeofenceResponse toResponse(Geofence geofence) {
        return new GeofenceResponse(
                geofence.getId(),
                geofence.getExternalId(),
                geofence.getName(),
                geofence.getType(),
                geofence.getCoordinatesJson(),
                geofence.getAlertOnEnter(),
                geofence.getAlertOnExit(),
                geofence.getAssignedTeams(),
                geofence.getSyncedAt(),
                geofence.getCreatedAt(),
                geofence.getUpdatedAt()
        );
    }
}

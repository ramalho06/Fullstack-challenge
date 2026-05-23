package com.media4all.tracking.external.geofence;

import java.util.List;

public interface ExternalGeofenceGateway {

    List<ExternalGeofenceDto> fetchAllGeofences();
}

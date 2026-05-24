package com.media4all.tracking.geofence;

import com.media4all.tracking.geofence.dto.GeofenceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GeofenceQueryService {

    private final GeofenceRepository geofenceRepository;
    private final GeofenceMapper geofenceMapper;

    public GeofenceQueryService(GeofenceRepository geofenceRepository, GeofenceMapper geofenceMapper) {
        this.geofenceRepository = geofenceRepository;
        this.geofenceMapper = geofenceMapper;
    }

    @Transactional(readOnly = true)
    public Page<GeofenceResponse> findGeofences(GeofenceType type, Pageable pageable) {
        return geofenceRepository.findAll(typeEquals(type), pageable)
                .map(geofenceMapper::toResponse);
    }

    private Specification<Geofence> typeEquals(GeofenceType type) {
        return (root, query, criteriaBuilder) ->
                type == null ? null : criteriaBuilder.equal(root.get("type"), type);
    }
}

package com.media4all.tracking.geofence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GeofenceRepository extends JpaRepository<Geofence, String>, JpaSpecificationExecutor<Geofence> {

    Optional<Geofence> findByExternalId(String externalId);

    boolean existsByExternalId(String externalId);
}

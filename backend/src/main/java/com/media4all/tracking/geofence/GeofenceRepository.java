package com.media4all.tracking.geofence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GeofenceRepository extends JpaRepository<Geofence, String> {

    Optional<Geofence> findByExternalId(String externalId);

    boolean existsByExternalId(String externalId);
}

package com.media4all.tracking.geofence;

import com.media4all.tracking.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "geofences",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_geofences_external_id", columnNames = "external_id")
        },
        indexes = {
                @Index(name = "idx_geofences_external_id", columnList = "external_id"),
                @Index(name = "idx_geofences_type", columnList = "type")
        }
)
public class Geofence extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, length = 80)
    private String id;

    @Column(name = "external_id", nullable = false, length = 80)
    private String externalId;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GeofenceType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String coordinatesJson;

    @Column(nullable = false)
    private Boolean alertOnEnter;

    @Column(nullable = false)
    private Boolean alertOnExit;

    @Column(length = 255)
    private String assignedTeams;

    @Column(nullable = false)
    private Instant syncedAt;
}

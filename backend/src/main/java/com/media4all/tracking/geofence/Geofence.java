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

import java.time.Instant;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeofenceType getType() {
        return type;
    }

    public void setType(GeofenceType type) {
        this.type = type;
    }

    public String getCoordinatesJson() {
        return coordinatesJson;
    }

    public void setCoordinatesJson(String coordinatesJson) {
        this.coordinatesJson = coordinatesJson;
    }

    public Boolean getAlertOnEnter() {
        return alertOnEnter;
    }

    public void setAlertOnEnter(Boolean alertOnEnter) {
        this.alertOnEnter = alertOnEnter;
    }

    public Boolean getAlertOnExit() {
        return alertOnExit;
    }

    public void setAlertOnExit(Boolean alertOnExit) {
        this.alertOnExit = alertOnExit;
    }

    public String getAssignedTeams() {
        return assignedTeams;
    }

    public void setAssignedTeams(String assignedTeams) {
        this.assignedTeams = assignedTeams;
    }

    public Instant getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(Instant syncedAt) {
        this.syncedAt = syncedAt;
    }
}

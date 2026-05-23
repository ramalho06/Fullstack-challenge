package com.media4all.tracking.agent;

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

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "agents",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_agents_external_id", columnNames = "external_id")
        },
        indexes = {
                @Index(name = "idx_agents_external_id", columnList = "external_id"),
                @Index(name = "idx_agents_status", columnList = "status"),
                @Index(name = "idx_agents_active", columnList = "active")
        }
)
public class Agent extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, length = 80)
    private String id;

    @Column(name = "external_id", nullable = false, length = 80)
    private String externalId;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private AgentRole role;

    @Column(length = 100)
    private String team;

    @Column(length = 30)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(nullable = false)
    private Boolean active;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AgentStatus status;

    @Column(precision = 5, scale = 2)
    private BigDecimal battery;

    private Instant lastSeen;

    @Column(precision = 10, scale = 7)
    private BigDecimal currentLatitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal currentLongitude;

    @Column(length = 255)
    private String currentAddress;

    @Column(precision = 10, scale = 2)
    private BigDecimal currentAccuracy;

    @Column(precision = 10, scale = 2)
    private BigDecimal currentSpeed;

    private Instant currentLocationUpdatedAt;

    private Instant externalCreatedAt;

    private Instant externalUpdatedAt;

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

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(AgentRole role) {
        this.role = role;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
    }

    public void setBattery(BigDecimal battery) {
        this.battery = battery;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void setExternalCreatedAt(Instant externalCreatedAt) {
        this.externalCreatedAt = externalCreatedAt;
    }

    public void setExternalUpdatedAt(Instant externalUpdatedAt) {
        this.externalUpdatedAt = externalUpdatedAt;
    }
}

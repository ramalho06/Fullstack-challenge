package com.media4all.tracking.checkin;

import com.media4all.tracking.agent.Agent;
import com.media4all.tracking.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "checkins",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_checkins_external_event_id", columnNames = "external_event_id")
        },
        indexes = {
                @Index(name = "idx_checkins_agent_occurred_at", columnList = "agent_id, occurred_at"),
                @Index(name = "idx_checkins_type", columnList = "type"),
                @Index(name = "idx_checkins_source", columnList = "source"),
                @Index(name = "idx_checkins_external_event_id", columnList = "external_event_id")
        }
)
public class CheckIn extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, length = 80)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CheckInType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CheckInSource source;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 255)
    private String address;

    @Column(precision = 10, scale = 2)
    private BigDecimal accuracy;

    @Column(precision = 10, scale = 2)
    private BigDecimal speed;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(precision = 12, scale = 2)
    private BigDecimal distanceFromPrevious;

    @Column(length = 120)
    private String externalEventId;

    @Column(nullable = false)
    private Instant occurredAt;

    private Instant syncedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public CheckInType getType() {
        return type;
    }

    public void setType(CheckInType type) {
        this.type = type;
    }

    public CheckInSource getSource() {
        return source;
    }

    public void setSource(CheckInSource source) {
        this.source = source;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(BigDecimal accuracy) {
        this.accuracy = accuracy;
    }

    public BigDecimal getSpeed() {
        return speed;
    }

    public void setSpeed(BigDecimal speed) {
        this.speed = speed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getDistanceFromPrevious() {
        return distanceFromPrevious;
    }

    public void setDistanceFromPrevious(BigDecimal distanceFromPrevious) {
        this.distanceFromPrevious = distanceFromPrevious;
    }

    public String getExternalEventId() {
        return externalEventId;
    }

    public void setExternalEventId(String externalEventId) {
        this.externalEventId = externalEventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Instant getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(Instant syncedAt) {
        this.syncedAt = syncedAt;
    }
}

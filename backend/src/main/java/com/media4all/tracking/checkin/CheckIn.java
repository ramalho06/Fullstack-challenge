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
}

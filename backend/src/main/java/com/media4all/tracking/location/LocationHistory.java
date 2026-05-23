package com.media4all.tracking.location;

import com.media4all.tracking.agent.Agent;
import com.media4all.tracking.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
        name = "location_history",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_location_history_external_event_id", columnNames = "external_event_id")
        },
        indexes = {
                @Index(name = "idx_location_history_agent_recorded_at", columnList = "agent_id, recorded_at"),
                @Index(name = "idx_location_history_source", columnList = "source")
        }
)
public class LocationHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 255)
    private String address;

    @Column(precision = 10, scale = 2)
    private BigDecimal accuracy;

    @Column(precision = 10, scale = 2)
    private BigDecimal speed;

    @Column(precision = 5, scale = 2)
    private BigDecimal battery;

    @Column(nullable = false)
    private Instant recordedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LocationSource source;

    @Column(length = 120)
    private String externalEventId;
}

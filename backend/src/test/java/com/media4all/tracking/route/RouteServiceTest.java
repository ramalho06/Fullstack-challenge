package com.media4all.tracking.route;

import com.media4all.tracking.agent.AgentRepository;
import com.media4all.tracking.common.exception.ResourceNotFoundException;
import com.media4all.tracking.common.geo.GeoDistanceCalculator;
import com.media4all.tracking.location.LocationHistory;
import com.media4all.tracking.location.LocationHistoryRepository;
import com.media4all.tracking.location.LocationSource;
import com.media4all.tracking.route.dto.RouteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RouteServiceTest {

    private final List<LocationHistory> storedPoints = new ArrayList<>();
    private boolean agentExists = true;
    private Instant capturedStart;
    private Instant capturedEnd;
    private RouteService service;

    @BeforeEach
    void setUp() {
        storedPoints.clear();
        agentExists = true;
        capturedStart = null;
        capturedEnd = null;
        service = new RouteService(
                agentRepositoryProxy(),
                locationHistoryRepositoryProxy(),
                new DistanceCalculatorFake()
        );
    }

    @Test
    void throwsWhenAgentDoesNotExist() {
        agentExists = false;

        assertThatThrownBy(() -> service.getDailyRoute("missing-agent", LocalDate.parse("2026-05-22")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Agent not found");
    }

    @Test
    void returnsEmptyRouteWhenAgentExistsButHasNoPoints() {
        RouteResponse response = service.getDailyRoute("seed_agent_001", LocalDate.parse("2026-05-22"));

        assertThat(response.agentId()).isEqualTo("seed_agent_001");
        assertThat(response.date()).isEqualTo(LocalDate.parse("2026-05-22"));
        assertThat(response.totalDistanceMeters()).isEqualByComparingTo("0.00");
        assertThat(response.points()).isEmpty();
    }

    @Test
    void searchesUsingAmericaSaoPauloDayIntervalConvertedToInstant() {
        service.getDailyRoute("seed_agent_001", LocalDate.parse("2026-05-22"));

        assertThat(capturedStart).isEqualTo(Instant.parse("2026-05-22T03:00:00Z"));
        assertThat(capturedEnd).isEqualTo(Instant.parse("2026-05-23T03:00:00Z"));
    }

    @Test
    void returnsOrderedPointsAndCalculatesDistances() {
        storedPoints.add(point(
                "-23.5520",
                "-46.6350",
                "2026-05-22T06:20:00Z",
                "20",
                LocationSource.EVENT_SYNC
        ));
        storedPoints.add(point(
                "-23.5505",
                "-46.6333",
                "2026-05-22T06:00:00Z",
                "8.5",
                LocationSource.GPS_SYNC
        ));
        storedPoints.add(point(
                "-23.5510",
                "-46.6340",
                "2026-05-22T06:10:00Z",
                null,
                LocationSource.MANUAL_CHECKIN
        ));

        RouteResponse response = service.getDailyRoute("seed_agent_001", LocalDate.parse("2026-05-22"));

        assertThat(response.points()).hasSize(3);
        assertThat(response.points())
                .extracting(point -> point.timestamp().toString())
                .containsExactly(
                        "2026-05-22T06:00:00Z",
                        "2026-05-22T06:10:00Z",
                        "2026-05-22T06:20:00Z"
                );
        assertThat(response.points())
                .extracting("source")
                .containsExactly(LocationSource.GPS_SYNC, LocationSource.MANUAL_CHECKIN, LocationSource.EVENT_SYNC);
        assertThat(response.points())
                .extracting("distanceFromPreviousMeters")
                .containsExactly(
                        BigDecimal.valueOf(0).setScale(2),
                        BigDecimal.valueOf(100).setScale(2),
                        BigDecimal.valueOf(200).setScale(2)
                );
        assertThat(response.totalDistanceMeters()).isEqualByComparingTo("300.00");
    }

    @Test
    void ignoresPointsWithAccuracyGreaterThanFiftyAndAcceptsNullAccuracy() {
        storedPoints.add(point(
                "-23.5505",
                "-46.6333",
                "2026-05-22T06:00:00Z",
                "8.5",
                LocationSource.GPS_SYNC
        ));
        storedPoints.add(point(
                "-23.5510",
                "-46.6340",
                "2026-05-22T06:10:00Z",
                "51",
                LocationSource.GPS_SYNC
        ));
        storedPoints.add(point(
                "-23.5520",
                "-46.6350",
                "2026-05-22T06:20:00Z",
                null,
                LocationSource.EVENT_SYNC
        ));

        RouteResponse response = service.getDailyRoute("seed_agent_001", LocalDate.parse("2026-05-22"));

        assertThat(response.points()).hasSize(2);
        assertThat(response.points())
                .extracting(point -> point.timestamp().toString())
                .containsExactly("2026-05-22T06:00:00Z", "2026-05-22T06:20:00Z");
        assertThat(response.totalDistanceMeters()).isEqualByComparingTo("100.00");
    }

    private AgentRepository agentRepositoryProxy() {
        return (AgentRepository) Proxy.newProxyInstance(
                AgentRepository.class.getClassLoader(),
                new Class[]{AgentRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "existsById" -> agentExists;
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private LocationHistoryRepository locationHistoryRepositoryProxy() {
        return (LocationHistoryRepository) Proxy.newProxyInstance(
                LocationHistoryRepository.class.getClassLoader(),
                new Class[]{LocationHistoryRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByAgentIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc" -> {
                        capturedStart = (Instant) args[1];
                        capturedEnd = (Instant) args[2];
                        yield List.copyOf(storedPoints);
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private LocationHistory point(
            String latitude,
            String longitude,
            String recordedAt,
            String accuracy,
            LocationSource source
    ) {
        LocationHistory point = new LocationHistory();
        point.setLatitude(new BigDecimal(latitude));
        point.setLongitude(new BigDecimal(longitude));
        point.setAddress("Address " + recordedAt);
        point.setAccuracy(accuracy == null ? null : new BigDecimal(accuracy));
        point.setSpeed(BigDecimal.ZERO);
        point.setRecordedAt(Instant.parse(recordedAt));
        point.setSource(source);
        return point;
    }

    private static class DistanceCalculatorFake extends GeoDistanceCalculator {

        private int calls;

        @Override
        public BigDecimal distanceInMeters(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
            calls++;
            return BigDecimal.valueOf(calls * 100L).setScale(2);
        }
    }
}

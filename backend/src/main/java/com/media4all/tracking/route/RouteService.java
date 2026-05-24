package com.media4all.tracking.route;

import com.media4all.tracking.agent.AgentRepository;
import com.media4all.tracking.common.exception.ResourceNotFoundException;
import com.media4all.tracking.common.geo.GeoDistanceCalculator;
import com.media4all.tracking.location.LocationHistory;
import com.media4all.tracking.location.LocationHistoryRepository;
import com.media4all.tracking.route.dto.RoutePointResponse;
import com.media4all.tracking.route.dto.RouteResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RouteService {

    private static final ZoneId ROUTE_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final BigDecimal MAX_ROUTE_ACCURACY = BigDecimal.valueOf(50);
    private static final BigDecimal ZERO_METERS = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final AgentRepository agentRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final GeoDistanceCalculator geoDistanceCalculator;

    public RouteService(
            AgentRepository agentRepository,
            LocationHistoryRepository locationHistoryRepository,
            GeoDistanceCalculator geoDistanceCalculator
    ) {
        this.agentRepository = agentRepository;
        this.locationHistoryRepository = locationHistoryRepository;
        this.geoDistanceCalculator = geoDistanceCalculator;
    }

    @Transactional(readOnly = true)
    public RouteResponse getDailyRoute(String agentId, LocalDate date) {
        if (!agentRepository.existsById(agentId)) {
            throw new ResourceNotFoundException("Agent", agentId);
        }

        Instant start = date.atStartOfDay(ROUTE_ZONE).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ROUTE_ZONE).toInstant();

        List<LocationHistory> validPoints = locationHistoryRepository
                .findByAgentIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        agentId,
                        start,
                        end
                )
                .stream()
                .filter(this::hasAcceptableAccuracy)
                .sorted(Comparator.comparing(LocationHistory::getRecordedAt))
                .toList();

        return buildResponse(agentId, date, validPoints);
    }

    private RouteResponse buildResponse(String agentId, LocalDate date, List<LocationHistory> validPoints) {
        List<RoutePointResponse> points = new ArrayList<>();
        BigDecimal totalDistance = ZERO_METERS;
        LocationHistory previous = null;

        for (LocationHistory current : validPoints) {
            BigDecimal distanceFromPrevious = previous == null
                    ? ZERO_METERS
                    : geoDistanceCalculator.distanceInMeters(
                    previous.getLatitude(),
                    previous.getLongitude(),
                    current.getLatitude(),
                    current.getLongitude()
            );

            totalDistance = totalDistance.add(distanceFromPrevious);
            points.add(toPointResponse(current, distanceFromPrevious));
            previous = current;
        }

        return new RouteResponse(
                agentId,
                date,
                totalDistance.setScale(2, RoundingMode.HALF_UP),
                points
        );
    }

    private boolean hasAcceptableAccuracy(LocationHistory point) {
        return point.getAccuracy() == null || point.getAccuracy().compareTo(MAX_ROUTE_ACCURACY) <= 0;
    }

    private RoutePointResponse toPointResponse(LocationHistory point, BigDecimal distanceFromPrevious) {
        return new RoutePointResponse(
                point.getLatitude(),
                point.getLongitude(),
                point.getAddress(),
                point.getAccuracy(),
                point.getSpeed(),
                point.getRecordedAt(),
                point.getSource(),
                distanceFromPrevious.setScale(2, RoundingMode.HALF_UP)
        );
    }
}

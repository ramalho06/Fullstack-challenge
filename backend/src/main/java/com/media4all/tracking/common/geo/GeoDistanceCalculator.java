package com.media4all.tracking.common.geo;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class GeoDistanceCalculator {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    public BigDecimal distanceInMeters(
            BigDecimal lat1,
            BigDecimal lon1,
            BigDecimal lat2,
            BigDecimal lon2
    ) {
        validateCoordinates(lat1, lon1, lat2, lon2);

        if (lat1.compareTo(lat2) == 0 && lon1.compareTo(lon2) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        double lat1Radians = Math.toRadians(lat1.doubleValue());
        double lat2Radians = Math.toRadians(lat2.doubleValue());
        double deltaLat = Math.toRadians(lat2.subtract(lat1).doubleValue());
        double deltaLon = Math.toRadians(lon2.subtract(lon1).doubleValue());

        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1Radians) * Math.cos(lat2Radians)
                * Math.pow(Math.sin(deltaLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return BigDecimal.valueOf(EARTH_RADIUS_METERS * c)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateCoordinates(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null) {
            throw new IllegalArgumentException("lat1 must not be null");
        }
        if (lon1 == null) {
            throw new IllegalArgumentException("lon1 must not be null");
        }
        if (lat2 == null) {
            throw new IllegalArgumentException("lat2 must not be null");
        }
        if (lon2 == null) {
            throw new IllegalArgumentException("lon2 must not be null");
        }
    }
}

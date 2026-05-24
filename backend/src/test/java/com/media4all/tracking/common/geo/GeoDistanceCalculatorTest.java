package com.media4all.tracking.common.geo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeoDistanceCalculatorTest {

    private final GeoDistanceCalculator calculator = new GeoDistanceCalculator();

    @Test
    void returnsZeroForSamePoint() {
        BigDecimal distance = calculator.distanceInMeters(
                BigDecimal.valueOf(-23.5505),
                BigDecimal.valueOf(-46.6333),
                BigDecimal.valueOf(-23.5505),
                BigDecimal.valueOf(-46.6333)
        );

        assertThat(distance).isEqualByComparingTo("0.00");
        assertThat(distance.scale()).isEqualTo(2);
    }

    @Test
    void returnsApproximateDistanceBetweenKnownPoints() {
        BigDecimal distance = calculator.distanceInMeters(
                BigDecimal.valueOf(-23.5505),
                BigDecimal.valueOf(-46.6333),
                BigDecimal.valueOf(-23.5510),
                BigDecimal.valueOf(-46.6340)
        );

        assertThat(distance).isCloseTo(BigDecimal.valueOf(90.48), within(BigDecimal.valueOf(1.00)));
        assertThat(distance.scale()).isEqualTo(2);
    }

    @Test
    void throwsWhenAnyCoordinateIsNull() {
        assertThatThrownBy(() -> calculator.distanceInMeters(
                null,
                BigDecimal.valueOf(-46.6333),
                BigDecimal.valueOf(-23.5510),
                BigDecimal.valueOf(-46.6340)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("lat1 must not be null");
    }

    private static org.assertj.core.data.Offset<BigDecimal> within(BigDecimal value) {
        return org.assertj.core.data.Offset.offset(value);
    }
}

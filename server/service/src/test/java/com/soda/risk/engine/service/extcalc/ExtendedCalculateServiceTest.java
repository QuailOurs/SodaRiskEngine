package com.soda.risk.engine.service.extcalc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.common.cache.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ExtendedCalculateServiceTest {

    private RedisCacheService cache;
    private ExtendedCalculateService service;

    @BeforeEach
    void setUp() {
        cache = new RedisCacheService(new ObjectMapper(), null);
        service = new ExtendedCalculateService(cache);
    }

    @Test
    void calculatesYearOnYearRingAndZeroBaselinesFromTestData() {
        long timestamp = Instant.parse("2026-08-24T00:00:00Z").toEpochMilli();
        long previous = timestamp - 86_400_000L;
        String formatted = LocalDateTime.ofInstant(Instant.ofEpochMilli(previous), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        cache.set("feature:10:" + formatted, "80");
        cache.set("feature:last:10", "50");

        assertThat(service.comparedWithTheSame(10, timestamp, 100, "day")).isCloseTo(25.0, within(0.0001));
        assertThat(service.comparedWithTheRing(10, timestamp, 75)).isCloseTo(50.0, within(0.0001));
        cache.set("feature:last:11", "0");
        assertThat(service.comparedWithTheRing(11, timestamp, 2)).isEqualTo(100.0);
    }

    @Test
    void evaluatesStoredStandardDeviationAndRejectsMissingOrInvalidStatistics() {
        cache.hSet("feature:stats:1", "mean", "100");
        cache.hSet("feature:stats:1", "stddev", "10");

        assertThat(service.isInStandardDeviationRange(1, 0, 119, 2)).isTrue();
        assertThat(service.isInStandardDeviationRange(1, 0, 121, 2)).isFalse();
        assertThat(service.isInStandardDeviationRange(2, 0, 100, 2)).isFalse();
        assertThat(service.isInStandardDeviationRange(1, 0, 100, -1)).isFalse();
    }

    @Test
    void calculatesGeographicAndTimeDistances() {
        assertThat(service.latitudeAndLongitudeDistance(31.2304, 121.4737, 31.2304, 121.4737)).isZero();
        assertThat(service.latitudeAndLongitudeDistance(31.2304, 121.4737, 39.9042, 116.4074))
                .isBetween(1_000.0, 1_200.0);
        assertThat(service.dataTimeDistance(10_000, 3_500)).isEqualTo(6);
    }
}

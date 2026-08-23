package com.soda.risk.engine.service.extcalc;

import com.soda.risk.engine.common.cache.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExtendedCalculateServiceTest {
    private RedisCacheService cache;
    private ExtendedCalculateService service;

    @BeforeEach
    void setUp() {
        cache = mock(RedisCacheService.class);
        service = new ExtendedCalculateService(cache);
    }

    @Test
    void calculatesRingComparison() {
        when(cache.get("feature:last:7")).thenReturn("80");
        assertEquals(25.0, service.comparedWithTheRing(7, 0, 100), 0.001);
    }

    @Test
    void missingHistoricalValueHasNoChange() {
        assertEquals(0.0, service.comparedWithTheRing(7, 0, 100));
    }

    @Test
    void checksStandardDeviationFromStoredStatistics() {
        when(cache.hGetAll("feature:stats:7")).thenReturn(Map.of("mean", "100", "stddev", "10"));
        assertTrue(service.isInStandardDeviationRange(7, 0, 119.9, 2));
        assertFalse(service.isInStandardDeviationRange(7, 0, 121, 2));
    }

    @Test
    void missingOrMalformedStatisticsFailClosed() {
        assertFalse(service.isInStandardDeviationRange(7, 0, 100, 2));
        when(cache.hGetAll("feature:stats:7")).thenReturn(Map.of("mean", "bad", "stddev", "10"));
        assertFalse(service.isInStandardDeviationRange(7, 0, 100, 2));
    }

    @Test
    void calculatesDistanceAndTimeBoundaries() {
        assertEquals(0.0, service.latitudeAndLongitudeDistance(1, 2, 1, 2));
        assertEquals(2, service.dataTimeDistance(1_000, 3_999));
    }
}

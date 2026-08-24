package com.soda.risk.engine.core.strategy.feature;

import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FeatureServiceTest {

    @Test
    void groupsFeatureConfigurationByTypeAndMergesHandlerResults() {
        RedisCacheService cache = mock(RedisCacheService.class);
        FeatureHandler base = mock(FeatureHandler.class);
        FeatureHandler statistics = mock(FeatureHandler.class);
        when(base.supports("base")).thenReturn(true);
        when(statistics.supports("statistics")).thenReturn(true);
        when(base.queryFeatures(any(), any())).thenReturn(Map.of("requestIp", "192.0.2.1"));
        when(statistics.queryFeatures(any(), any())).thenReturn(Map.of("loginCount", 7.0));

        Map<Object, Object> configs = new LinkedHashMap<>();
        configs.put("requestIp", "base:ip");
        configs.put("loginCount", "statistics:count:10m:userId");
        when(cache.hGetAll(RedisKeyConstants.SCENE_PREFIX + "login:features")).thenReturn(configs);

        FeatureService service = new FeatureService(cache, List.of(base, statistics));
        Map<String, Object> result = service.queryAllFeatures(Map.of("ip", "192.0.2.1"), "login");

        assertThat(result).containsEntry("requestIp", "192.0.2.1").containsEntry("loginCount", 7.0);
        verify(base).queryFeatures(any(), argThat(entries -> entries.size() == 1
                && "ip".equals(entries.get(0).getValue())));
        verify(statistics).queryFeatures(any(), argThat(entries -> entries.size() == 1
                && "count:10m:userId".equals(entries.get(0).getValue())));
    }

    @Test
    void handlesMissingConfigurationsAndUnsupportedSingleFeatureTypes() {
        RedisCacheService cache = mock(RedisCacheService.class);
        when(cache.hGetAll(any())).thenReturn(Map.of());
        FeatureService service = new FeatureService(cache, List.of());

        assertThat(service.queryAllFeatures(Map.of(), "missing")).isEmpty();
        assertThat(service.queryFeature("unknown", "unsupported", Map.of())).isNull();
    }

    @Test
    void executesIndependentFeatureTypesInParallel() throws Exception {
        RedisCacheService cache = mock(RedisCacheService.class);
        FeatureHandler first = mock(FeatureHandler.class);
        FeatureHandler second = mock(FeatureHandler.class);
        when(first.supports("first")).thenReturn(true);
        when(second.supports("second")).thenReturn(true);
        CountDownLatch bothStarted = new CountDownLatch(2);
        when(first.queryFeatures(any(), any())).thenAnswer(ignored -> awaitBoth(bothStarted, "one", 1));
        when(second.queryFeatures(any(), any())).thenAnswer(ignored -> awaitBoth(bothStarted, "two", 2));
        when(cache.hGetAll(any())).thenReturn(new LinkedHashMap<>(Map.of(
                "one", "first:one", "two", "second:two")));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            FeatureService service = new FeatureService(cache, List.of(first, second), executor, 500);

            FeatureQueryResult result = service.queryFeatures(Map.of(), "parallel");

            assertThat(result.values()).containsEntry("one", 1).containsEntry("two", 2);
            assertThat(result.degraded()).isFalse();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void usesOneGlobalDeadlineAndReportsTimedOutTypes() {
        RedisCacheService cache = mock(RedisCacheService.class);
        FeatureHandler first = slowHandler("first");
        FeatureHandler second = slowHandler("second");
        when(cache.hGetAll(any())).thenReturn(new LinkedHashMap<>(Map.of(
                "one", "first:one", "two", "second:two")));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            FeatureService service = new FeatureService(cache, List.of(first, second), executor, 60);
            long startedAt = System.nanoTime();

            FeatureQueryResult result = service.queryFeatures(Map.of(), "timeout");

            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            assertThat(result.degraded()).isTrue();
            assertThat(result.timedOutTypes()).containsExactlyInAnyOrder("first", "second");
            assertThat(elapsedMs).isLessThan(250L);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void isolatesFailedFeatureTypeAndKeepsSuccessfulValues() {
        RedisCacheService cache = mock(RedisCacheService.class);
        FeatureHandler failed = mock(FeatureHandler.class);
        FeatureHandler successful = mock(FeatureHandler.class);
        when(failed.supports("failed")).thenReturn(true);
        when(successful.supports("successful")).thenReturn(true);
        when(failed.queryFeatures(any(), any())).thenThrow(new IllegalStateException("upstream down"));
        when(successful.queryFeatures(any(), any())).thenReturn(Map.of("risk", 9));
        when(cache.hGetAll(any())).thenReturn(new LinkedHashMap<>(Map.of(
                "broken", "failed:broken", "risk", "successful:risk")));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            FeatureService service = new FeatureService(cache, List.of(failed, successful), executor, 500);

            FeatureQueryResult result = service.queryFeatures(Map.of(), "degraded");

            assertThat(result.values()).containsEntry("risk", 9);
            assertThat(result.failedTypes()).containsExactly("failed");
            assertThat(result.degraded()).isTrue();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void reportsRejectedFeatureJobsAsDegradedWithoutThrowing() {
        RedisCacheService cache = mock(RedisCacheService.class);
        FeatureHandler handler = mock(FeatureHandler.class);
        ExecutorService executor = mock(ExecutorService.class);
        when(handler.supports("algorithm")).thenReturn(true);
        when(cache.hGetAll(any())).thenReturn(Map.of("score", "algorithm:model-score"));
        when(executor.submit(any(Callable.class)))
                .thenThrow(new RejectedExecutionException("queue full"));
        FeatureService service = new FeatureService(cache, List.of(handler), executor, 100);

        FeatureQueryResult result = service.queryFeatures(Map.of("userId", "u-1"), "busy");

        assertThat(result.values()).isEmpty();
        assertThat(result.failedTypes()).containsExactly("algorithm");
        assertThat(result.timedOutTypes()).isEmpty();
        assertThat(result.degraded()).isTrue();
        verify(handler, never()).queryFeatures(any(), any());
    }

    private Map<String, Object> awaitBoth(CountDownLatch latch, String key, int value) throws Exception {
        latch.countDown();
        if (!latch.await(300, TimeUnit.MILLISECONDS)) throw new IllegalStateException("not parallel");
        return Map.of(key, value);
    }

    private FeatureHandler slowHandler(String type) {
        FeatureHandler handler = mock(FeatureHandler.class);
        when(handler.supports(type)).thenReturn(true);
        when(handler.queryFeatures(any(), any())).thenAnswer(ignored -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return Map.of(type, true);
        });
        return handler;
    }
}

package com.soda.risk.engine.common.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 监控门面。
 * 使用 Micrometer 与 Prometheus 输出运行指标
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorFacade {

    private final MeterRegistry meterRegistry;

    private static final Map<String, Timer> TIMER_CACHE = new ConcurrentHashMap<>();
    private static MeterRegistry staticRegistry;

    @PostConstruct
    public void init() {
        staticRegistry = meterRegistry;
    }

    /**
     * 记录耗时
     */
    public static void insert(String key, long costMs) {
        if (staticRegistry == null) return;
        try {
            Timer timer = TIMER_CACHE.computeIfAbsent(key, k ->
                Timer.builder("risk_engine_cost")
                    .tag("key", k)
                    .description("Engine operation cost")
                    .register(staticRegistry)
            );
            timer.record(java.time.Duration.ofMillis(costMs));
        } catch (Exception e) {
            log.warn("Monitor insert failed for key={}: {}", key, e.getMessage());
        }
    }

    /**
     * 监控线程池
     */
    public static void esToMonitor(String key, ThreadPoolExecutor executor) {
        if (staticRegistry == null || executor == null) return;
        try {
            staticRegistry.gauge("risk_engine_thread_pool_active", 
                java.util.Collections.singletonList(
                    io.micrometer.core.instrument.Tag.of("key", key)),
                executor, ThreadPoolExecutor::getActiveCount);
            staticRegistry.gauge("risk_engine_thread_pool_queue_size",
                java.util.Collections.singletonList(
                    io.micrometer.core.instrument.Tag.of("key", key)),
                executor, e -> e.getQueue().size());
        } catch (Exception e) {
            log.warn("Monitor esToMonitor failed for key={}: {}", key, e.getMessage());
        }
    }

    /**
     * 增加计数器
     */
    public static void increment(String key) {
        if (staticRegistry == null) return;
        try {
            staticRegistry.counter("risk_engine_count", "key", key).increment();
        } catch (Exception e) {
            log.warn("Monitor increment failed for key={}: {}", key, e.getMessage());
        }
    }
}

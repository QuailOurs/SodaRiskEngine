package com.soda.risk.engine.core.strategy.feature;

import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 特征服务
 * 统一管理特征查询，消除对多个外部特征服务的直接依赖
 */
@Slf4j
@Service
public class FeatureService {

    private final RedisCacheService redisCacheService;
    private final List<FeatureHandler> featureHandlers;
    private final ExecutorService queryExecutor;
    private final long timeoutMs;

    @Autowired
    public FeatureService(RedisCacheService redisCacheService,
                          List<FeatureHandler> featureHandlers,
                          @Qualifier("featureQueryExecutor") ExecutorService queryExecutor,
                          @Value("${soda.engine.feature-timeout-ms:200}") long timeoutMs) {
        this.redisCacheService = redisCacheService;
        this.featureHandlers = List.copyOf(featureHandlers);
        this.queryExecutor = queryExecutor;
        this.timeoutMs = Math.max(1L, timeoutMs);
    }

    /** 供轻量单元测试和非 Spring 嵌入场景使用。 */
    public FeatureService(RedisCacheService redisCacheService, List<FeatureHandler> featureHandlers) {
        this(redisCacheService, featureHandlers, ForkJoinPool.commonPool(), 200L);
    }

    /**
     * 查询场景关联的所有特征
     */
    public Map<String, Object> queryAllFeatures(Map<String, Object> dataMap, String sceneKey) {
        return queryFeatures(dataMap, sceneKey).values();
    }

    /**
     * 按特征类型并行执行查询。所有任务共享一个全局超时预算，避免按任务逐个等待导致
     * 最坏耗时放大为“特征类型数 × 单任务超时”。
     */
    public FeatureQueryResult queryFeatures(Map<String, Object> dataMap, String sceneKey) {
        long start = System.currentTimeMillis();
        Map<String, Object> features = new LinkedHashMap<>();
        Set<String> failedTypes = new LinkedHashSet<>();
        Set<String> timedOutTypes = new LinkedHashSet<>();

        try {
            // 从Redis获取场景关联的特征配置
            Map<Object, Object> featureConfigs = redisCacheService.hGetAll(
                    RedisKeyConstants.SCENE_PREFIX + sceneKey + ":features");

            if (featureConfigs == null || featureConfigs.isEmpty()) {
                log.debug("No features found for sceneKey={}", sceneKey);
                long cost = System.currentTimeMillis() - start;
                MonitorFacade.insert("[feature]QueryAll", cost);
                return FeatureQueryResult.empty(cost);
            }

            // 按特征类型分组查询
            Map<String, List<Map.Entry<Object, Object>>> groupedFeatures = featureConfigs.entrySet().stream()
                    .sorted(Comparator.comparing(entry -> String.valueOf(entry.getKey())))
                    .map(entry -> new AbstractMap.SimpleEntry<Object, Object>(
                            entry.getKey(), getFeatureConfig(String.valueOf(entry.getValue()))))
                    .collect(Collectors.groupingBy(e -> getFeatureType(String.valueOf(
                                    featureConfigs.get(e.getKey()))),
                            LinkedHashMap::new, Collectors.toList()));

            Map<String, Future<Map<String, Object>>> futures = new LinkedHashMap<>();
            Map<String, Object> queryData = Collections.unmodifiableMap(new LinkedHashMap<>(dataMap));
            for (Map.Entry<String, List<Map.Entry<Object, Object>>> entry : groupedFeatures.entrySet()) {
                String featureType = entry.getKey();
                FeatureHandler handler = getHandler(featureType);
                if (handler == null) {
                    failedTypes.add(featureType);
                    log.warn("No handler found for configured featureType={}, sceneKey={}", featureType, sceneKey);
                    continue;
                }
                try {
                    futures.put(featureType, queryExecutor.submit(() ->
                            handler.queryFeatures(queryData, entry.getValue())));
                } catch (RejectedExecutionException e) {
                    failedTypes.add(featureType);
                    log.warn("Feature query rejected, sceneKey={}, featureType={}", sceneKey, featureType);
                }
            }

            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
            for (Map.Entry<String, Future<Map<String, Object>>> entry : futures.entrySet()) {
                String featureType = entry.getKey();
                Future<Map<String, Object>> future = entry.getValue();
                try {
                    Map<String, Object> typeFeatures;
                    if (future.isDone()) {
                        typeFeatures = future.get();
                    } else {
                        long remaining = deadline - System.nanoTime();
                        if (remaining <= 0) throw new TimeoutException("global feature deadline exceeded");
                        typeFeatures = future.get(remaining, TimeUnit.NANOSECONDS);
                    }
                    if (typeFeatures != null) features.putAll(typeFeatures);
                } catch (TimeoutException e) {
                    timedOutTypes.add(featureType);
                    future.cancel(true);
                    log.warn("Feature query timed out, sceneKey={}, featureType={}, timeoutMs={}",
                            sceneKey, featureType, timeoutMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    failedTypes.add(featureType);
                    future.cancel(true);
                } catch (ExecutionException | CancellationException e) {
                    failedTypes.add(featureType);
                    log.warn("Feature query failed, sceneKey={}, featureType={}", sceneKey, featureType,
                            e instanceof ExecutionException ? e.getCause() : e);
                }
            }

        } catch (Exception e) {
            failedTypes.add("configuration");
            log.error("queryFeatures failed, sceneKey={}", sceneKey, e);
        }

        long cost = System.currentTimeMillis() - start;
        MonitorFacade.insert("[feature]QueryAll", cost);
        return new FeatureQueryResult(features, failedTypes, timedOutTypes, cost);
    }

    /**
     * 查询单个特征
     */
    public Object queryFeature(String featureName, String featureType, Map<String, Object> params) {
        FeatureHandler handler = getHandler(featureType);
        if (handler == null) {
            log.warn("No handler found for featureType={}", featureType);
            return null;
        }
        return handler.querySingle(params);
    }

    private FeatureHandler getHandler(String featureType) {
        return featureHandlers.stream()
                .filter(h -> h.supports(featureType))
                .findFirst()
                .orElse(null);
    }

    private String getFeatureType(String config) {
        int separator = config.indexOf(':');
        return separator < 0 ? "default" : config.substring(0, separator);
    }

    private String getFeatureConfig(String config) {
        int separator = config.indexOf(':');
        return separator < 0 ? config : config.substring(separator + 1);
    }
}

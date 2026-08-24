package com.soda.risk.engine.core.strategy.feature;

import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * 一次场景特征查询的结构化结果。
 * 除特征值外保留失败和超时类型，供决策链路做降级标记和问题定位。
 */
public record FeatureQueryResult(
        Map<String, Object> values,
        Set<String> failedTypes,
        Set<String> timedOutTypes,
        long costMs) {

    public FeatureQueryResult {
        values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
        failedTypes = Set.copyOf(failedTypes);
        timedOutTypes = Set.copyOf(timedOutTypes);
    }

    public boolean degraded() {
        return !failedTypes.isEmpty() || !timedOutTypes.isEmpty();
    }

    public static FeatureQueryResult empty(long costMs) {
        return new FeatureQueryResult(Map.of(), Set.of(), Set.of(), costMs);
    }
}

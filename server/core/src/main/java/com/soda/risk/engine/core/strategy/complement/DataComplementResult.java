package com.soda.risk.engine.core.strategy.complement;

import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.LinkedHashMap;

/** 数据补全结果及其降级信息。 */
public record DataComplementResult(Map<String, Object> data, Set<String> failedHandlers, long costMs) {

    public DataComplementResult {
        data = Collections.unmodifiableMap(new LinkedHashMap<>(data));
        failedHandlers = Set.copyOf(failedHandlers);
    }

    public boolean degraded() {
        return !failedHandlers.isEmpty();
    }
}

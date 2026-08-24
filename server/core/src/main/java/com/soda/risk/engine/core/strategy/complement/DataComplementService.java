package com.soda.risk.engine.core.strategy.complement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

/**
 * 数据补全编排器。处理器之间相互隔离，单个外部数据源异常不会终止整个决策。
 */
@Slf4j
@Service
public class DataComplementService {

    private final List<DataComplementHandler> handlers;

    public DataComplementService(List<DataComplementHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    public DataComplementResult complete(String sceneKey, Map<String, Object> source) {
        long start = System.currentTimeMillis();
        Map<String, Object> completed = new LinkedHashMap<>(source);
        Set<String> failures = new LinkedHashSet<>();
        for (DataComplementHandler handler : handlers) {
            try {
                Map<String, Object> snapshot = Collections.unmodifiableMap(new LinkedHashMap<>(completed));
                if (!handler.supports(sceneKey, snapshot)) continue;
                Map<String, Object> additions = handler.complement(sceneKey, snapshot);
                if (additions != null) completed.putAll(additions);
            } catch (Exception e) {
                failures.add(handler.name());
                log.warn("Data complement failed, sceneKey={}, handler={}", sceneKey, handler.name(), e);
            }
        }
        return new DataComplementResult(completed, failures, System.currentTimeMillis() - start);
    }
}

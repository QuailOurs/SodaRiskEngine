package com.soda.risk.engine.core.strategy.feature;

import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 累计特征处理器
 * 处理同比、环比等统计计算
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CalculationFeatureHandler implements FeatureHandler {

    private final RedisCacheService redisCacheService;

    @Override
    public boolean supports(String featureType) {
        return "calculation".equalsIgnoreCase(featureType);
    }

    @Override
    public Map<String, Object> queryFeatures(Map<String, Object> dataMap,
                                              List<Map.Entry<Object, Object>> featureConfigs) {
        long start = System.currentTimeMillis();
        Map<String, Object> results = new HashMap<>();

        try {
            for (Map.Entry<Object, Object> config : featureConfigs) {
                String featureName = (String) config.getKey();
                String configValue = (String) config.getValue();
                Object result = calculateFeature(dataMap, configValue);
                results.put(featureName, result);
            }
        } catch (Exception e) {
            log.error("CalculationFeatureHandler queryFeatures failed", e);
        }

        MonitorFacade.insert("[feature]Calculation", System.currentTimeMillis() - start);
        return results;
    }

    @Override
    public Object querySingle(Map<String, Object> params) {
        return calculateFeature(params, (String) params.get("config"));
    }

    private Object calculateFeature(Map<String, Object> dataMap, String config) {
        try {
            // 解析配置中的计算规则
            // 支持：同环比、累计值、均值等
            String calcKey = RedisKeyConstants.FEATURE_CALCULATION + config;
            String value = redisCacheService.get(calcKey);
            if (value != null) {
                return Double.parseDouble(value);
            }
            return 0.0;
        } catch (Exception e) {
            log.error("calculateFeature failed, config={}", config, e);
            return 0.0;
        }
    }
}

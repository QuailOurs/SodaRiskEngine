package com.soda.risk.engine.core.strategy.feature;

import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 统计特征处理器 - 处理累计特征
 * 支持同环比、累计值、均值等统计计算
 * 负责读取和计算窗口统计特征
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsFeatureHandler implements FeatureHandler {

    private final RedisCacheService redisCacheService;

    @Override
    public boolean supports(String featureType) {
        return "statistics".equalsIgnoreCase(featureType);
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
                Object result = calculateStatistics(dataMap, configValue);
                results.put(featureName, result);
            }
        } catch (Exception e) {
            log.error("StatisticsFeatureHandler queryFeatures failed", e);
        }

        MonitorFacade.insert("[feature]Statistics", System.currentTimeMillis() - start);
        return results;
    }

    @Override
    public Object querySingle(Map<String, Object> params) {
        return calculateStatistics(params, (String) params.get("config"));
    }

    /**
     * 计算统计特征
     * config格式: "statType:timeWindow:keyField"
     * statType: count, sum, avg, yoy(同比), mom(环比)
     */
    private Object calculateStatistics(Map<String, Object> dataMap, String config) {
        try {
            String[] parts = config.split(":");
            if (parts.length < 3) return 0.0;

            String statType = parts[0];
            String timeWindow = parts[1];
            String keyField = parts[2];

            String keyValue = String.valueOf(dataMap.getOrDefault(keyField, ""));
            String redisKey = RedisKeyConstants.FEATURE_CALCULATION + statType + ":" + timeWindow + ":" + keyValue;

            String value = redisCacheService.get(redisKey);
            if (value != null) {
                return Double.parseDouble(value);
            }

            // 如果Redis中没有，尝试从统计数据源查询
            return queryFromDataSource(statType, timeWindow, keyValue);
        } catch (Exception e) {
            log.error("calculateStatistics failed, config={}", config, e);
            return 0.0;
        }
    }

    private double queryFromDataSource(String statType, String timeWindow, String keyValue) {
        // TODO: 通过统计数据源适配器查询窗口指标
        // 数据源实现由部署方按需提供
        return 0.0;
    }
}

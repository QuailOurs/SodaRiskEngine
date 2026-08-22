package com.soda.risk.engine.core.strategy.feature;

import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 画像特征处理器
 * 查询用户画像数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PortraitFeatureHandler implements FeatureHandler {

    private final RedisCacheService redisCacheService;

    @Override
    public boolean supports(String featureType) {
        return "portrait".equalsIgnoreCase(featureType);
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
                Object result = queryPortraitValue(dataMap, configValue);
                results.put(featureName, result);
            }
        } catch (Exception e) {
            log.error("PortraitFeatureHandler queryFeatures failed", e);
        }

        MonitorFacade.insert("[feature]Portrait", System.currentTimeMillis() - start);
        return results;
    }

    @Override
    public Object querySingle(Map<String, Object> params) {
        return queryPortraitValue(params, (String) params.get("config"));
    }

    private Object queryPortraitValue(Map<String, Object> dataMap, String config) {
        try {
            // 从Redis中查询画像数据
            String portraitKey = RedisKeyConstants.FEATURE_PORTRAIT + config;
            String value = redisCacheService.get(portraitKey);
            if (value != null) {
                return value;
            }
            return "";
        } catch (Exception e) {
            log.error("queryPortraitValue failed, config={}", config, e);
            return "";
        }
    }
}

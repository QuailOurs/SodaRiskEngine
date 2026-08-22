package com.soda.risk.engine.core.strategy.feature;

import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 认证特征处理器 - 处理实名认证、人脸识别等认证类特征
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFeatureHandler implements FeatureHandler {

    private final RedisCacheService redisCacheService;

    @Override
    public boolean supports(String featureType) {
        return "auth".equalsIgnoreCase(featureType) || "authentication".equalsIgnoreCase(featureType);
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
                Object result = queryAuthFeature(dataMap, configValue);
                results.put(featureName, result);
            }
        } catch (Exception e) {
            log.error("AuthFeatureHandler queryFeatures failed", e);
        }

        MonitorFacade.insert("[feature]Auth", System.currentTimeMillis() - start);
        return results;
    }

    @Override
    public Object querySingle(Map<String, Object> params) {
        return queryAuthFeature(params, (String) params.get("config"));
    }

    private Object queryAuthFeature(Map<String, Object> dataMap, String config) {
        try {
            String userId = (String) dataMap.getOrDefault("userId", "");
            if (userId.isEmpty()) return 0;

            // 查询Redis中的认证状态
            String authKey = RedisKeyConstants.FEATURE_AUTH + config + ":" + userId;
            String value = redisCacheService.get(authKey);

            if (value != null) {
                return Integer.parseInt(value);
            }
            return 0;
        } catch (Exception e) {
            log.error("queryAuthFeature failed, config={}", config, e);
            return 0;
        }
    }
}

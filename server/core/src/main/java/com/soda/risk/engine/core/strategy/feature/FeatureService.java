package com.soda.risk.engine.core.strategy.feature;

import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 特征服务
 * 统一管理特征查询，消除对多个外部特征服务的直接依赖
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureService {

    private final RedisCacheService redisCacheService;
    private final List<FeatureHandler> featureHandlers;

    /**
     * 查询场景关联的所有特征
     */
    public Map<String, Object> queryAllFeatures(Map<String, Object> dataMap, String sceneKey) {
        long start = System.currentTimeMillis();
        Map<String, Object> features = new HashMap<>();

        try {
            // 从Redis获取场景关联的特征配置
            Map<Object, Object> featureConfigs = redisCacheService.hGetAll(
                    RedisKeyConstants.SCENE_PREFIX + sceneKey + ":features");

            if (featureConfigs == null || featureConfigs.isEmpty()) {
                log.debug("No features found for sceneKey={}", sceneKey);
                return features;
            }

            // 按特征类型分组查询
            Map<String, List<Map.Entry<Object, Object>>> groupedFeatures = featureConfigs.entrySet().stream()
                    .map(entry -> new AbstractMap.SimpleEntry<Object, Object>(
                            entry.getKey(), getFeatureConfig(String.valueOf(entry.getValue()))))
                    .collect(Collectors.groupingBy(e -> getFeatureType(String.valueOf(
                            featureConfigs.get(e.getKey())))));

            for (Map.Entry<String, List<Map.Entry<Object, Object>>> entry : groupedFeatures.entrySet()) {
                String featureType = entry.getKey();
                FeatureHandler handler = getHandler(featureType);
                if (handler != null) {
                    Map<String, Object> typeFeatures = handler.queryFeatures(dataMap, entry.getValue());
                    features.putAll(typeFeatures);
                }
            }

        } catch (Exception e) {
            log.error("queryAllFeatures failed, sceneKey={}", sceneKey, e);
        }

        MonitorFacade.insert("[feature]QueryAll", System.currentTimeMillis() - start);
        return features;
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

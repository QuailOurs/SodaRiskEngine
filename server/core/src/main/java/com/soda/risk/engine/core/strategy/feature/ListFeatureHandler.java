package com.soda.risk.engine.core.strategy.feature;

import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 名单特征处理器
 * 查询Redis中的黑白名单数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListFeatureHandler implements FeatureHandler {

    private final RedisCacheService redisCacheService;

    @Override
    public boolean supports(String featureType) {
        return "list".equalsIgnoreCase(featureType) || "nameList".equalsIgnoreCase(featureType);
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
                // 从配置中解析查询参数
                Object result = querySingleFeature(dataMap, configValue);
                results.put(featureName, result);
            }
        } catch (Exception e) {
            log.error("ListFeatureHandler queryFeatures failed", e);
        }

        MonitorFacade.insert("[feature]List", System.currentTimeMillis() - start);
        return results;
    }

    @Override
    public Object querySingle(Map<String, Object> params) {
        return querySingleFeature(params, (String) params.get("config"));
    }

    private Object querySingleFeature(Map<String, Object> dataMap, String config) {
        try {
            // 解析配置，获取名单Key
            // 简化实现：从Redis中查询名单
            String listKey = RedisKeyConstants.BLACK_WHITE_LIST + config;
            Set<String> members = redisCacheService.sMembers(listKey);
            if (members == null || members.isEmpty()) {
                return 0;
            }

            // 检查数据中的值是否在名单中
            String checkValue = (String) dataMap.get(config);
            if (checkValue != null && members.contains(checkValue)) {
                return 1;
            }
            return 0;
        } catch (Exception e) {
            log.error("querySingleFeature failed, config={}", config, e);
            return 0;
        }
    }
}

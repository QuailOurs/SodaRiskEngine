package com.soda.risk.engine.core.strategy.feature;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 直接从请求参数读取基础特征。 */
@Component
public class BaseFeatureHandler implements FeatureHandler {

    @Override
    public boolean supports(String featureType) {
        return "base".equalsIgnoreCase(featureType) || "default".equalsIgnoreCase(featureType);
    }

    @Override
    public Map<String, Object> queryFeatures(Map<String, Object> dataMap,
                                             List<Map.Entry<Object, Object>> featureConfigs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> config : featureConfigs) {
            String outputKey = String.valueOf(config.getKey());
            String inputKey = String.valueOf(config.getValue());
            if (dataMap.containsKey(inputKey)) result.put(outputKey, dataMap.get(inputKey));
        }
        return result;
    }

    @Override
    public Object querySingle(Map<String, Object> params) {
        Object config = params.get("config");
        return config == null ? null : params.get(config.toString());
    }
}

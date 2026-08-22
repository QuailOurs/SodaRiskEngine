package com.soda.risk.engine.core.strategy.feature;

import com.soda.risk.engine.common.monitor.MonitorFacade;
import com.soda.risk.engine.core.thirdparty.ThirdPartyServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 算法特征处理器 - 处理算法模型输出的特征
 * 如登录保护模型、注册保护模型、恶意注册模型等
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlgorithmFeatureHandler implements FeatureHandler {

    private final ThirdPartyServiceFactory thirdPartyServiceFactory;

    @Override
    public boolean supports(String featureType) {
        return "algorithm".equalsIgnoreCase(featureType) || "model".equalsIgnoreCase(featureType);
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
                Object result = querySingleFeature(dataMap, configValue);
                results.put(featureName, result);
            }
        } catch (Exception e) {
            log.error("AlgorithmFeatureHandler queryFeatures failed", e);
        }

        MonitorFacade.insert("[feature]Algorithm", System.currentTimeMillis() - start);
        return results;
    }

    @Override
    public Object querySingle(Map<String, Object> params) {
        return querySingleFeature(params, (String) params.get("config"));
    }

    private Object querySingleFeature(Map<String, Object> dataMap, String config) {
        try {
            // config格式: "modelKey:inputField1,inputField2:outputField"
            String[] parts = config.split(":");
            if (parts.length < 2) return 0.0;

            String modelKey = parts[0];

            // 调用算法模型服务
            Map<String, Object> params = new HashMap<>(dataMap);
            params.put("modelKey", modelKey);
            params.put("serviceType", "algorithm");

            Map<String, Object> result = thirdPartyServiceFactory.query("algorithm", params);
            if (result != null && result.containsKey("score")) {
                return result.get("score");
            }

            return 0.0;
        } catch (Exception e) {
            log.error("querySingleFeature failed, config={}", config, e);
            return 0.0;
        }
    }
}

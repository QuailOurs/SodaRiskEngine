package com.soda.risk.engine.service.impl;

import com.soda.risk.engine.api.dto.FeatureResult;
import com.soda.risk.engine.api.interfaces.IFeatureService;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import com.soda.risk.engine.core.strategy.feature.FeatureHandler;
import com.soda.risk.engine.core.strategy.feature.FeatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 特征服务实现 - 对外暴露的特征查询接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureServiceImpl implements IFeatureService {

    private final FeatureService featureService;
    private final List<FeatureHandler> featureHandlers;

    @Override
    public FeatureResult queryFeature(String featureName, Map<String, Object> params) {
        long start = System.currentTimeMillis();
        try {
            String featureType = (String) params.getOrDefault("featureType", "default");
            Object value = featureService.queryFeature(featureName, featureType, params);

            return FeatureResult.builder()
                    .featureName(featureName)
                    .value(value)
                    .featureType(featureType)
                    .valid(value != null)
                    .costMs(System.currentTimeMillis() - start)
                    .build();

        } catch (Exception e) {
            log.error("Query feature failed, name={}", featureName, e);
            return FeatureResult.builder()
                    .featureName(featureName)
                    .valid(false)
                    .costMs(System.currentTimeMillis() - start)
                    .build();
        }
    }

    @Override
    public List<FeatureResult> queryFeatures(List<String> featureNames, Map<String, Object> params) {
        long start = System.currentTimeMillis();
        List<FeatureResult> results = new ArrayList<>();

        try {
            String sceneKey = (String) params.getOrDefault("sceneKey", "");
            Map<String, Object> allFeatures = featureService.queryAllFeatures(params, sceneKey);

            for (String featureName : featureNames) {
                Object value = allFeatures.get(featureName);
                results.add(FeatureResult.builder()
                        .featureName(featureName)
                        .value(value)
                        .valid(value != null)
                        .costMs(System.currentTimeMillis() - start)
                        .build());
            }

        } catch (Exception e) {
            log.error("Query features failed", e);
        }

        MonitorFacade.insert("[service]QueryFeatures", System.currentTimeMillis() - start);
        return results;
    }
}

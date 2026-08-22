package com.soda.risk.engine.api.interfaces;

import com.soda.risk.engine.api.dto.FeatureResult;

import java.util.List;
import java.util.Map;

/**
 * 特征服务接口
 */
public interface IFeatureService {

    /**
     * 查询单个特征
     */
    FeatureResult queryFeature(String featureName, Map<String, Object> params);

    /**
     * 批量查询特征
     */
    List<FeatureResult> queryFeatures(List<String> featureNames, Map<String, Object> params);
}

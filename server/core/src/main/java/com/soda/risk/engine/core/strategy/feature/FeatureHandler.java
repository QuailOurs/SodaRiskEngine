package com.soda.risk.engine.core.strategy.feature;

import java.util.List;
import java.util.Map;

/**
 * 特征处理器接口 - 统一特征查询抽象
 * 统一抽象名单、画像、统计和算法等特征查询
 */
public interface FeatureHandler {

    /**
     * 是否支持该特征类型
     */
    boolean supports(String featureType);

    /**
     * 批量查询特征
     * @param dataMap 原始数据
     * @param featureConfigs 特征配置列表
     * @return 特征名 -> 特征值
     */
    Map<String, Object> queryFeatures(Map<String, Object> dataMap,
                                       List<Map.Entry<Object, Object>> featureConfigs);

    /**
     * 查询单个特征
     */
    Object querySingle(Map<String, Object> params);
}

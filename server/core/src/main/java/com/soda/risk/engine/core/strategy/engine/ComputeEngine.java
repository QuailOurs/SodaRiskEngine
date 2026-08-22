package com.soda.risk.engine.core.strategy.engine;

import com.soda.risk.engine.api.dto.StrategyHitResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 计算引擎抽象类
 * 由 Spring 管理依赖和生命周期
 */
@Slf4j
public abstract class ComputeEngine {

    /**
     * 执行策略计算 - 模板方法
     */
    public StrategyHitResult execute(String data, String sceneKey, String openKey) {
        long start = System.currentTimeMillis();
        try {
            // 1. 预处理
            Map<String, Object> dataMap = pretreatment(data, sceneKey, openKey);
            if (dataMap == null) {
                return StrategyHitResult.builder()
                        .hit(false)
                        .costMs(System.currentTimeMillis() - start)
                        .build();
            }

            // 2. 特征计算
            Map<String, Object> features = computeFeatures(dataMap, sceneKey);

            // 3. 规则匹配
            StrategyHitResult result = matchRules(features, dataMap, sceneKey, openKey);
            result.setCostMs(System.currentTimeMillis() - start);
            return result;

        } catch (Exception e) {
            log.error("ComputeEngine execute failed, sceneKey={}, openKey={}", sceneKey, openKey, e);
            return StrategyHitResult.builder()
                    .hit(false)
                    .costMs(System.currentTimeMillis() - start)
                    .build();
        }
    }

    /**
     * 预处理 - 数据解析和校验
     */
    protected abstract Map<String, Object> pretreatment(String data, String sceneKey, String openKey);

    /**
     * 特征计算 - 查询和计算所有特征值
     */
    protected abstract Map<String, Object> computeFeatures(Map<String, Object> dataMap, String sceneKey);

    /**
     * 规则匹配 - 执行规则表达式计算
     */
    protected abstract StrategyHitResult matchRules(Map<String, Object> features,
                                                     Map<String, Object> dataMap,
                                                     String sceneKey, String openKey);
}

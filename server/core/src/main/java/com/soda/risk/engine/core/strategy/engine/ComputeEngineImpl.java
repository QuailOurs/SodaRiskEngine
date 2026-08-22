package com.soda.risk.engine.core.strategy.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.api.dto.RuleHitResult;
import com.soda.risk.engine.api.dto.StrategyHitResult;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.common.util.CommonUtils;
import com.soda.risk.engine.core.strategy.feature.FeatureService;
import com.soda.risk.engine.core.strategy.rule.RuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 计算引擎实现
 * 整合原策略引擎的完整计算流程：
 * 1. 预处理 (数据解析+IP补全等数据补全)
 * 2. 特征计算 (查询场景关联的特征)
 * 3. 参数组装 (组装规则评估所需参数)
 * 4. 规则匹配 (执行规则表达式评估)
 * 5. 命中响应 (生成详细命中结果)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ComputeEngineImpl extends ComputeEngine {

    private final ObjectMapper objectMapper;
    private final FeatureService featureService;
    private final RuleService ruleService;
    private final RedisCacheService redisCacheService;

    @Override
    protected Map<String, Object> pretreatment(String data, String sceneKey, String openKey) {
        long start = System.currentTimeMillis();
        try {
            if (data == null || data.trim().isEmpty()) {
                log.warn("Pretreatment data is empty, sceneKey={}, openKey={}", sceneKey, openKey);
                return null;
            }

            Map<String, Object> dataMap = objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
            if (dataMap.isEmpty()) {
                log.warn("Pretreatment data map is empty, sceneKey={}", sceneKey);
                return null;
            }

            // 生成TraceId
            dataMap.put("traceId", UUID.randomUUID().toString().replace("-", ""));
            dataMap.put("serverIp", CommonUtils.getServerIp());
            dataMap.put("processTime", System.currentTimeMillis());

            // 数据补全：补充 IP 归属地等外部数据
            dataMap = complementData(dataMap, sceneKey);

            // 参数类型标准化，确保表达式获得正确的数据类型
            dataMap = normalizeParameters(dataMap);

            MonitorFacade.insert("[engine]Pretreatment", System.currentTimeMillis() - start);
            return dataMap;

        } catch (Exception e) {
            log.error("Pretreatment failed, sceneKey={}, data={}", sceneKey, data, e);
            MonitorFacade.insert("[engine]Pretreatment_Error", System.currentTimeMillis() - start);
            return null;
        }
    }

    @Override
    protected Map<String, Object> computeFeatures(Map<String, Object> dataMap, String sceneKey) {
        long start = System.currentTimeMillis();
        try {
            // 查询场景关联的所有特征
            Map<String, Object> features = featureService.queryAllFeatures(dataMap, sceneKey);
            MonitorFacade.insert("[engine]ComputeFeatures", System.currentTimeMillis() - start);
            return features;
        } catch (Exception e) {
            log.error("ComputeFeatures failed, sceneKey={}", sceneKey, e);
            MonitorFacade.insert("[engine]ComputeFeatures_Error", System.currentTimeMillis() - start);
            return Collections.emptyMap();
        }
    }

    @Override
    protected StrategyHitResult matchRules(Map<String, Object> features,
                                            Map<String, Object> dataMap,
                                            String sceneKey, String openKey) {
        long start = System.currentTimeMillis();
        try {
            // 合并原始数据和特征数据
            Map<String, Object> allData = new HashMap<>(dataMap);
            allData.putAll(features);

            // 执行规则匹配
            List<RuleHitResult> hitRules = ruleService.evaluateRules(allData, sceneKey);

            // 判断是否命中
            boolean hit = hitRules.stream().anyMatch(RuleHitResult::isHit);

            // 获取命中规则的处置信息
            List<Long> hitRuleIds = hitRules.stream()
                    .filter(RuleHitResult::isHit)
                    .map(RuleHitResult::getRuleId)
                    .collect(Collectors.toList());

            MonitorFacade.insert("[engine]MatchRules", System.currentTimeMillis() - start);

            // 构建可解释的规则命中响应
            return StrategyHitResult.builder()
                    .strategyId(getStrategyId(sceneKey))
                    .strategyName(getStrategyName(sceneKey))
                    .openId(openKey)
                    .sceneKey(sceneKey)
                    .hit(hit)
                    .hitRules(hitRules)
                    .traceId((String) dataMap.get("traceId"))
                    .userId((String) dataMap.getOrDefault("userId", ""))
                    .extra(buildHitExtra(hitRules, hitRuleIds, features, dataMap))
                    .build();

        } catch (Exception e) {
            log.error("MatchRules failed, sceneKey={}", sceneKey, e);
            MonitorFacade.insert("[engine]MatchRules_Error", System.currentTimeMillis() - start);
            return StrategyHitResult.builder()
                    .hit(false)
                    .sceneKey(sceneKey)
                    .openId(openKey)
                    .build();
        }
    }

    // ======================== 数据补全 ========================

    /**
     * 数据补全 - 补充IP归属地等外部数据
     * 可通过适配器接入 IP、设备等外部数据源
     */
    private Map<String, Object> complementData(Map<String, Object> dataMap, String sceneKey) {
        try {
            // IP归属地补全（如果有IP字段）
            String ip = (String) dataMap.getOrDefault("ip", dataMap.getOrDefault("userIp", null));
            if (ip != null && !ip.isEmpty()) {
                dataMap.put("ipProvince", "");
                dataMap.put("ipCity", "");
                dataMap.put("ipIsp", "");
                // TODO: 接入IP查询服务（如百度IP库、淘宝IP库等）
            }
            // 设备指纹补全
            String deviceId = (String) dataMap.getOrDefault("deviceId", null);
            if (deviceId != null) {
                dataMap.put("deviceRisk", "NORMAL");
                // TODO: 接入设备指纹服务
            }
        } catch (Exception e) {
            log.warn("Data complement failed, sceneKey={}", sceneKey, e);
        }
        return dataMap;
    }

    /**
     * 参数类型标准化
     * 将文本形式的数值转换为表达式可计算的数字类型
     */
    private Map<String, Object> normalizeParameters(Map<String, Object> dataMap) {
        Map<String, Object> normalized = new HashMap<>(dataMap.size());
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                String strVal = (String) value;
                // 尝试转为数字（用于Aviator表达式计算）
                if (isNumeric(strVal)) {
                    try {
                        normalized.put(entry.getKey(), Double.parseDouble(strVal));
                        continue;
                    } catch (NumberFormatException ignored) {}
                }
            }
            normalized.put(entry.getKey(), value);
        }
        return normalized;
    }

    /**
     * 构建命中响应的扩展信息
     * 汇总规则结果、参数快照和处置信息
     */
    private Map<String, Object> buildHitExtra(List<RuleHitResult> hitRules,
                                               List<Long> hitRuleIds,
                                               Map<String, Object> features,
                                               Map<String, Object> dataMap) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("hitRuleIds", hitRuleIds);
        extra.put("hitRuleCount", hitRuleIds.size());
        extra.put("totalRuleCount", hitRules.size());
        extra.put("featureKeys", features.keySet());
        // 提取用于解释决策结果的参数快照
        Map<String, Object> paramSnapshot = new HashMap<>();
        for (RuleHitResult rule : hitRules) {
            if (rule.isHit() && rule.getParamValues() != null) {
                paramSnapshot.putAll(rule.getParamValues());
            }
        }
        extra.put("paramSnapshot", paramSnapshot);
        return extra;
    }

    private Long getStrategyId(String sceneKey) {
        String strategyId = redisCacheService.get(RedisKeyConstants.SCENE_ROUTE_MAP + sceneKey);
        if (strategyId == null || strategyId.isBlank()) return 0L;
        try {
            return Long.valueOf(strategyId);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private String getStrategyName(String sceneKey) {
        Long strategyId = getStrategyId(sceneKey);
        if (strategyId == 0L) return sceneKey;
        String strategyName = redisCacheService.get(RedisKeyConstants.STRATEGY_PREFIX + strategyId + ":name");
        return strategyName == null || strategyName.isBlank() ? sceneKey : strategyName;
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i == 0 && (c == '-' || c == '+')) continue;
            if (c == '.' ) continue;
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
}

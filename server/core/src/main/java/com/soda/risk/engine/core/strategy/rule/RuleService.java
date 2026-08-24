package com.soda.risk.engine.core.strategy.rule;

import com.soda.risk.engine.api.dto.RuleHitResult;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import com.soda.risk.engine.common.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 规则计算服务
 * 使用 Aviator 表达式引擎执行规则
 */
@Slf4j
@Service("ruleEngineService")
@RequiredArgsConstructor
public class RuleService {

    private final RedisCacheService redisCacheService;
    private final RuleExpressionEvaluator expressionEvaluator;

    /**
     * 评估场景关联的所有规则
     * @param allData 合并后的数据（原始数据 + 特征数据）
     * @param sceneKey 场景标识
     * @return 规则命中结果列表
     */
    public List<RuleHitResult> evaluateRules(Map<String, Object> allData, String sceneKey) {
        long start = System.currentTimeMillis();
        List<RuleHitResult> results = new ArrayList<>();

        try {
            // 从Redis获取场景关联的规则列表
            Set<String> ruleIds = redisCacheService.sMembers(
                    RedisKeyConstants.SCENE_PREFIX + sceneKey + ":rules");

            if (ruleIds == null || ruleIds.isEmpty()) {
                log.debug("No rules found for sceneKey={}", sceneKey);
                return results;
            }

            // 逐个评估规则
            for (String ruleId : ruleIds) {
                Long parsedRuleId;
                try {
                    parsedRuleId = Long.parseLong(ruleId);
                } catch (NumberFormatException e) {
                    log.error("Invalid rule id: {}", ruleId);
                    results.add(RuleHitResult.builder()
                            .hit(false)
                            .detail("规则ID无效: " + ruleId)
                            .build());
                    continue;
                }
                try {
                    RuleHitResult result = evaluateSingleRule(allData, parsedRuleId);
                    results.add(result);
                } catch (Exception e) {
                    log.error("Evaluate rule failed, ruleId={}", ruleId, e);
                    results.add(RuleHitResult.builder()
                            .ruleId(parsedRuleId)
                            .hit(false)
                            .detail("规则评估异常: " + e.getMessage())
                            .build());
                }
            }

        } catch (Exception e) {
            log.error("evaluateRules failed, sceneKey={}", sceneKey, e);
        }

        MonitorFacade.insert("[rule]EvaluateAll", System.currentTimeMillis() - start);
        return results;
    }

    /**
     * 评估单个规则
     */
    public RuleHitResult evaluateSingleRule(Map<String, Object> data, Long ruleId) {
        long start = System.currentTimeMillis();

        try {
            // 从Redis获取规则配置
            Map<Object, Object> ruleConfig = redisCacheService.hGetAll(
                    RedisKeyConstants.RULE_PREFIX + ruleId);

            if (ruleConfig == null || ruleConfig.isEmpty()) {
                return RuleHitResult.builder()
                        .ruleId(ruleId)
                        .hit(false)
                        .detail("规则配置不存在")
                        .build();
            }

            String ruleName = (String) ruleConfig.getOrDefault("name", "");
            String ruleKey = (String) ruleConfig.getOrDefault("key", "");
            String expression = (String) ruleConfig.getOrDefault("expression", "");
            String ruleType = (String) ruleConfig.getOrDefault("type", "EXPRESSION");

            // 根据规则类型执行不同的评估逻辑
            boolean hit;
            String detail;

            switch (ruleType.toUpperCase()) {
                case "EXPRESSION":
                    hit = expressionEvaluator.evaluate(expression, data);
                    detail = hit ? "表达式匹配" : "表达式不匹配";
                    break;
                case "LIST":
                    hit = evaluateListRule(data, ruleConfig);
                    detail = hit ? "名单命中" : "名单未命中";
                    break;
                case "THRESHOLD":
                    hit = evaluateThresholdRule(data, ruleConfig);
                    detail = hit ? "阈值命中" : "阈值未命中";
                    break;
                default:
                    hit = false;
                    detail = "未知规则类型: " + ruleType;
            }

            MonitorFacade.insert("[rule]Evaluate_" + ruleId, System.currentTimeMillis() - start);

            return RuleHitResult.builder()
                    .ruleId(ruleId)
                    .ruleName(ruleName)
                    .ruleKey(ruleKey)
                    .hit(hit)
                    .detail(detail)
                    .paramValues(extractParamValues(data, ruleConfig))
                    .build();

        } catch (Exception e) {
            log.error("evaluateSingleRule failed, ruleId={}", ruleId, e);
            return RuleHitResult.builder()
                    .ruleId(ruleId)
                    .hit(false)
                    .detail("规则评估异常: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 评估名单规则
     */
    private boolean evaluateListRule(Map<String, Object> data, Map<Object, Object> ruleConfig) {
        String checkField = (String) ruleConfig.get("checkField");
        String listKey = (String) ruleConfig.get("listKey");

        if (checkField == null || listKey == null) return false;

        Object value = data.get(checkField);
        if (value == null) return false;

        String redisKey = RedisKeyConstants.BLACK_WHITE_LIST + listKey;
        return Boolean.TRUE.equals(redisCacheService.sIsMember(redisKey, value.toString()));
    }

    /**
     * 评估阈值规则
     */
    private boolean evaluateThresholdRule(Map<String, Object> data, Map<Object, Object> ruleConfig) {
        String checkField = (String) ruleConfig.get("checkField");
        String operator = (String) ruleConfig.get("operator");
        String threshold = (String) ruleConfig.get("threshold");

        if (checkField == null || operator == null || threshold == null) return false;

        Object value = data.get(checkField);
        if (value == null) return false;

        try {
            double numValue = Double.parseDouble(value.toString());
            double thresholdValue = Double.parseDouble(threshold);

            switch (operator) {
                case ">": return numValue > thresholdValue;
                case ">=": return numValue >= thresholdValue;
                case "<": return numValue < thresholdValue;
                case "<=": return numValue <= thresholdValue;
                case "==": return Math.abs(numValue - thresholdValue) < 0.0001;
                case "!=": return Math.abs(numValue - thresholdValue) >= 0.0001;
                default: return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Map<String, Object> extractParamValues(Map<String, Object> data, Map<Object, Object> ruleConfig) {
        Map<String, Object> paramValues = new HashMap<>();
        String checkField = (String) ruleConfig.get("checkField");
        if (checkField != null && data.containsKey(checkField)) {
            paramValues.put(checkField, data.get(checkField));
        }
        return paramValues;
    }
}

package com.soda.risk.engine.config.rule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.config.strategy.Strategy;
import com.soda.risk.engine.config.strategy.StrategyMapper;
import com.soda.risk.engine.config.strategy.StrategyRuleRelation;
import com.soda.risk.engine.config.strategy.StrategyRuleRelationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleServiceImpl extends ServiceImpl<RuleMapper, Rule> implements RuleService {

    private final RedisCacheService redisCacheService;
    private final StrategyRuleRelationMapper relationMapper;
    private final StrategyMapper strategyMapper;

    @Override
    public List<Rule> getByStrategyId(Long strategyId) {
        List<StrategyRuleRelation> relations = relationMapper.selectList(
                new LambdaQueryWrapper<StrategyRuleRelation>()
                        .eq(StrategyRuleRelation::getStrategyId, strategyId)
                        .orderByAsc(StrategyRuleRelation::getPriority)
                        .orderByAsc(StrategyRuleRelation::getId));
        if (relations.isEmpty()) return List.of();

        Map<Long, Rule> rulesById = listByIds(relations.stream()
                .map(StrategyRuleRelation::getRuleId)
                .toList()).stream().collect(Collectors.toMap(Rule::getId, rule -> rule));
        List<Rule> result = new ArrayList<>();
        for (StrategyRuleRelation relation : relations) {
            Rule rule = rulesById.get(relation.getRuleId());
            if (rule != null) result.add(rule);
        }
        return result;
    }

    @Override
    public void syncToRedis() {
        redisCacheService.delete(redisCacheService.keys(RedisKeyConstants.RULE_PREFIX + "*"));
        redisCacheService.delete(redisCacheService.keys(RedisKeyConstants.SCENE_PREFIX + "*:rules"));
        List<Rule> rules = list(new LambdaQueryWrapper<Rule>().eq(Rule::getState, 1));
        for (Rule rule : rules) {
            String key = RedisKeyConstants.RULE_PREFIX + rule.getId();
            redisCacheService.hSet(key, "name", defaultString(rule.getName()));
            redisCacheService.hSet(key, "key", defaultString(rule.getRuleKey()));
            redisCacheService.hSet(key, "expression", defaultString(rule.getExpression()));
            redisCacheService.hSet(key, "type", defaultString(rule.getRuleType(), "EXPRESSION"));
            redisCacheService.hSet(key, "sceneKey", defaultString(rule.getSceneKey()));
            redisCacheService.hSet(key, "operator", defaultString(rule.getRuleExpressOp()));
            redisCacheService.hSet(key, "threshold", defaultString(rule.getRuleExpressRight()));
        }

        Map<Long, Rule> activeRules = rules.stream().collect(Collectors.toMap(Rule::getId, rule -> rule));
        List<Strategy> strategies = strategyMapper.selectList(
                new LambdaQueryWrapper<Strategy>().in(Strategy::getState, 1, 2));
        Map<Long, Strategy> activeStrategies = strategies.stream()
                .collect(Collectors.toMap(Strategy::getId, strategy -> strategy));
        List<StrategyRuleRelation> relations = relationMapper.selectList(null);
        Map<String, Set<String>> sceneRuleIds = new LinkedHashMap<>();
        for (Strategy strategy : strategies) {
            if (strategy.getSceneKey() != null) {
                sceneRuleIds.computeIfAbsent(strategy.getSceneKey(), ignored -> new java.util.LinkedHashSet<>());
            }
        }
        for (StrategyRuleRelation relation : relations) {
            Strategy strategy = activeStrategies.get(relation.getStrategyId());
            if (strategy != null && activeRules.containsKey(relation.getRuleId()) && strategy.getSceneKey() != null) {
                sceneRuleIds.computeIfAbsent(strategy.getSceneKey(), ignored -> new java.util.LinkedHashSet<>())
                        .add(String.valueOf(relation.getRuleId()));
            }
        }
        sceneRuleIds.forEach((sceneKey, ruleIds) -> redisCacheService.replaceSet(
                RedisKeyConstants.SCENE_PREFIX + sceneKey + ":rules", ruleIds));
        log.info("Synced {} rules to Redis", rules.size());
    }

    private String defaultString(String value) {
        return defaultString(value, "");
    }

    private String defaultString(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }
}

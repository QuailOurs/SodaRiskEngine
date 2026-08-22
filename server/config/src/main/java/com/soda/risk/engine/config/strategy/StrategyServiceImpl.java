package com.soda.risk.engine.config.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 策略配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyServiceImpl extends ServiceImpl<StrategyMapper, Strategy> implements StrategyService {

    private final RedisCacheService redisCacheService;
    private final StrategyRuleRelationMapper relationMapper;

    @Override
    public Strategy getBySceneKey(String sceneKey) {
        return getOne(new LambdaQueryWrapper<Strategy>()
                .eq(Strategy::getSceneKey, sceneKey)
                .eq(Strategy::getState, 1)
                .last("LIMIT 1"));
    }

    @Override
    public void syncToRedis() {
        List<Strategy> strategies = list(new LambdaQueryWrapper<Strategy>()
                .eq(Strategy::getState, 1));
        for (Strategy strategy : strategies) {
            syncToRedis(strategy);
        }
        log.info("Synced {} strategies to Redis", strategies.size());
    }

    @Override
    public void syncToRedis(Long strategyId) {
        Strategy strategy = getById(strategyId);
        if (strategy != null) {
            syncToRedis(strategy);
        }
    }

    @Override
    public List<Long> getRuleIds(Long strategyId) {
        if (strategyId == null) return Collections.emptyList();
        return getRuleIdsMap(Collections.singleton(strategyId))
                .getOrDefault(strategyId, Collections.emptyList());
    }

    @Override
    public Map<Long, List<Long>> getRuleIdsMap(Collection<Long> strategyIds) {
        if (strategyIds == null || strategyIds.isEmpty()) return Collections.emptyMap();
        Map<Long, List<Long>> result = new LinkedHashMap<>();
        relationMapper.selectList(new LambdaQueryWrapper<StrategyRuleRelation>()
                        .in(StrategyRuleRelation::getStrategyId, strategyIds)
                        .orderByAsc(StrategyRuleRelation::getPriority)
                        .orderByAsc(StrategyRuleRelation::getId))
                .forEach(relation -> result
                        .computeIfAbsent(relation.getStrategyId(), ignored -> new java.util.ArrayList<>())
                        .add(relation.getRuleId()));
        return result;
    }

    @Override
    @Transactional
    public void replaceRules(Long strategyId, List<Long> ruleIds) {
        relationMapper.delete(new LambdaQueryWrapper<StrategyRuleRelation>()
                .eq(StrategyRuleRelation::getStrategyId, strategyId));
        if (ruleIds == null) return;
        int priority = 1;
        for (Long ruleId : new java.util.LinkedHashSet<>(ruleIds)) {
            if (ruleId == null) continue;
            StrategyRuleRelation relation = new StrategyRuleRelation();
            relation.setStrategyId(strategyId);
            relation.setRuleId(ruleId);
            relation.setPriority(priority++);
            relationMapper.insert(relation);
        }
    }

    private void syncToRedis(Strategy strategy) {
        String key = RedisKeyConstants.STRATEGY_PREFIX + strategy.getId();
        redisCacheService.setJson(key, strategy, 24, TimeUnit.HOURS);
        if (strategy.getSceneKey() != null) {
            redisCacheService.set(RedisKeyConstants.SCENE_ROUTE_MAP + strategy.getSceneKey(),
                    String.valueOf(strategy.getId()), 24, TimeUnit.HOURS);
        }
        redisCacheService.set(key + ":name", strategy.getName() == null ? "" : strategy.getName(),
                24, TimeUnit.HOURS);
    }
}

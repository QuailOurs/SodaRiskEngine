package com.soda.risk.engine.config.strategy;

import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 策略配置服务接口
 */
public interface StrategyService extends IService<Strategy> {

    /**
     * 根据场景Key查询策略
     */
    Strategy getBySceneKey(String sceneKey);

    /**
     * 同步策略配置到Redis
     */
    void syncToRedis();

    /**
     * 同步单个策略到Redis
     */
    void syncToRedis(Long strategyId);

    List<Long> getRuleIds(Long strategyId);

    Map<Long, List<Long>> getRuleIdsMap(Collection<Long> strategyIds);

    void replaceRules(Long strategyId, List<Long> ruleIds);
}

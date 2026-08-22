package com.soda.risk.engine.config.rule;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RuleService extends IService<Rule> {

    List<Rule> getByStrategyId(Long strategyId);

    void syncToRedis();
}

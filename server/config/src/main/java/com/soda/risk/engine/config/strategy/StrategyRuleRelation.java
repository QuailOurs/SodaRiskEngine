package com.soda.risk.engine.config.strategy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_strategy_rule_relation")
public class StrategyRuleRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long strategyId;
    private Long ruleId;
    private Integer priority;
}

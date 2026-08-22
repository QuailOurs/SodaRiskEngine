package com.soda.risk.engine.config.strategy;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import com.soda.risk.engine.config.rule.Rule;

/**
 * 策略实体
 */
@Data
@TableName("t_strategy")
public class Strategy {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String strategyKey;
    private String sceneKey;
    private Integer strategyType;
    private Integer type;
    private String expression;
    private String expressionRelation;
    private Integer priority;
    private Integer threshold;
    private BigDecimal score;
    private String returnCode;
    private String abilitySource;
    @TableField(exist = false)
    private List<Long> ruleIds;
    @TableField(exist = false)
    private List<Rule> rules;
    @TableField(exist = false)
    private String businessSideKey;
    @TableField(exist = false)
    private String typeName;
    @TableField(exist = false)
    private String stateName;
    @TableField(exist = false)
    private String expressionView;
    private Integer state;
    private String description;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

package com.soda.risk.engine.config.rule;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则实体
 */
@Data
@TableName("t_rule")
public class Rule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String ruleKey;
    private String sceneKey;
    private Integer type;
    private Long toolId;
    private Long srcParamId;
    private String destParamIdsJson;
    @TableField(exist = false)
    private List<Long> destParamIds;
    private Long featureId;
    private Long ruleExpressLeft;
    private String ruleExpressOp;
    private String ruleExpressRight;
    private String expression;
    private String extParam;
    private String ruleType;
    private Integer state;
    private String description;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String typeName;
    @TableField(exist = false)
    private String stateName;
    @TableField(exist = false)
    private String expressionView;
}

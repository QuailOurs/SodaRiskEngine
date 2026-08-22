package com.soda.risk.engine.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 单条策略的计算结果。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyMatchResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long strategyId;
    private String strategyName;
    private String strategyKey;
    /** 1-预上线，2-上线。 */
    private Integer state;
    private BigDecimal score;
    private String returnCode;
    private String abilitySource;
    private String expression;
    private List<RuleHitResult> rules;
}

package com.soda.risk.engine.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 规则命中结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleHitResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 规则ID */
    private Long ruleId;
    /** 规则名称 */
    private String ruleName;
    /** 规则Key */
    private String ruleKey;
    /** 是否命中 */
    private boolean hit;
    /** 命中详情 */
    private String detail;
    /** 参数值 */
    private Map<String, Object> paramValues;
}

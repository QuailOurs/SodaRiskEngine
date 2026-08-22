package com.soda.risk.engine.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 风险决策结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskDecisionResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** TraceId */
    private String traceId;
    /** 风险分数 */
    private int score;
    /** 风险等级 */
    private String riskLevel;
    /** 决策详情 */
    private Map<String, Object> detail;
    /** 原始数据 */
    private Map<String, Object> originalData;
    /** OpenId */
    private String openId;
    /** 业务类型 */
    private String businessType;
}

package com.soda.risk.engine.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 策略命中结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyHitResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 策略ID */
    private Long strategyId;
    /** 策略名称 */
    private String strategyName;
    /** 策略Key */
    private String strategyKey;
    /** 业务方OpenId */
    private String openId;
    /** 场景Key */
    private String sceneKey;
    /** 是否命中 */
    private boolean hit;
    /** 命中的规则列表 */
    private List<RuleHitResult> hitRules;
    /** 处置响应 */
    private DisposerResponse disposerResponse;
    /** 计算耗时(ms) */
    private long costMs;
    /** TraceId */
    private String traceId;
    /** 用户ID */
    private String userId;
    /** 参数 */
    private Map<String, Object> params;
    /** 扩展数据 */
    private Map<String, Object> extra;
}

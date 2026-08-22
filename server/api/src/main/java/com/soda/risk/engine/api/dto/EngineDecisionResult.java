package com.soda.risk.engine.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 一次场景决策的完整结果。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EngineDecisionResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String requestId;
    private String traceId;
    private String businessKey;
    private String sceneKey;
    /** HIT、PRE_HIT、NOT_HIT。 */
    private String status;
    private boolean hit;
    private BigDecimal score;
    private BigDecimal preScore;
    private List<String> returnCodes;
    private List<StrategyMatchResult> strategies;
    private List<StrategyMatchResult> preStrategies;
    private Map<String, Object> detail;
    private long configVersion;
    private long costMs;
}

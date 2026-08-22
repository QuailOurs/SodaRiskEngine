package com.soda.risk.engine.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 当前引擎内存配置快照状态。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EngineConfigStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private long version;
    private LocalDateTime loadedAt;
    private int sceneCount;
    private int strategyCount;
    private int ruleCount;
    private int relationCount;
}

package com.soda.risk.engine.config.riskdecision;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 风险配置实体
 */
@Data
@TableName("t_risk_config")
public class RiskConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String riskKey;
    private String businessType;
    private Integer riskLevel;
    private Integer scoreThreshold;
    private String disposition;
    private Integer state;
    private String description;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

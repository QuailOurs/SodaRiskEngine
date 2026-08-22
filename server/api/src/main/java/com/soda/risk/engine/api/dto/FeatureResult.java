package com.soda.risk.engine.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 特征查询结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 特征名称 */
    private String featureName;
    /** 特征值 */
    private Object value;
    /** 特征类型 */
    private String featureType;
    /** 是否有效 */
    private boolean valid;
    /** 查询耗时(ms) */
    private long costMs;
}

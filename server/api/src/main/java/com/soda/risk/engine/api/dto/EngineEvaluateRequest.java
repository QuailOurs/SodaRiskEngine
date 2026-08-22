package com.soda.risk.engine.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/** HTTP规则引擎单次决策请求。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EngineEvaluateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 调用方自定义请求号；为空时由引擎生成。 */
    private String requestId;

    /** 配置平台中的业务方标识。 */
    @NotBlank
    private String businessKey;

    /** 配置平台中的场景标识。 */
    @NotBlank
    private String sceneKey;

    /** 参与规则计算的原始字段与调用方已计算特征。 */
    @NotEmpty
    private Map<String, Object> data;

    /** 是否返回未命中规则和参数快照。 */
    @Builder.Default
    private boolean needDetail = false;
}

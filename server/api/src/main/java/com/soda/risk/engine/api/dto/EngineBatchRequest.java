package com.soda.risk.engine.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/** HTTP规则引擎批量决策请求。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EngineBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    @NotEmpty
    @Size(max = 100)
    private List<EngineEvaluateRequest> requests;
}

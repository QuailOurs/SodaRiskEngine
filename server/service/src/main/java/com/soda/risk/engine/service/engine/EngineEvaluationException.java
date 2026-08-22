package com.soda.risk.engine.service.engine;

import com.soda.risk.engine.common.enums.CodeEnum;
import lombok.Getter;

@Getter
public class EngineEvaluationException extends RuntimeException {

    private final CodeEnum code;

    public EngineEvaluationException(CodeEnum code, String message) {
        super(message);
        this.code = code;
    }
}

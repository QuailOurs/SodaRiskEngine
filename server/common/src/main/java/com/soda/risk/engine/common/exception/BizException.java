package com.soda.risk.engine.common.exception;

import com.soda.risk.engine.common.enums.CodeEnum;
import lombok.Getter;

/**
 * 业务异常 - 统一异常定义
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;
    private final CodeEnum codeEnum;

    public BizException(CodeEnum codeEnum) {
        super(codeEnum.getMsg());
        this.code = codeEnum.getCode();
        this.codeEnum = codeEnum;
    }

    public BizException(CodeEnum codeEnum, String message) {
        super(message);
        this.code = codeEnum.getCode();
        this.codeEnum = codeEnum;
    }

    public BizException(CodeEnum codeEnum, Throwable cause) {
        super(codeEnum.getMsg(), cause);
        this.code = codeEnum.getCode();
        this.codeEnum = codeEnum;
    }
}

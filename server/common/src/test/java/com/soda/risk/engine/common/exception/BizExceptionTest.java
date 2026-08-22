package com.soda.risk.engine.common.exception;

import com.soda.risk.engine.common.enums.CodeEnum;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BizExceptionTest {

    @Test
    void testConstructorWithCodeEnum() {
        BizException ex = new BizException(CodeEnum.SYSTEM_ERROR);
        assertEquals(-1, ex.getCode());
        assertEquals("系统异常", ex.getMessage());
        assertEquals(CodeEnum.SYSTEM_ERROR, ex.getCodeEnum());
    }

    @Test
    void testConstructorWithCodeEnumAndMsg() {
        BizException ex = new BizException(CodeEnum.PARAM_ERROR, "自定义错误消息");
        assertEquals(1001, ex.getCode());
        assertEquals("自定义错误消息", ex.getMessage());
    }

    @Test
    void testConstructorWithCodeEnumAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        BizException ex = new BizException(CodeEnum.THIRD_PARTY_ERROR, cause);
        assertEquals(5001, ex.getCode());
        assertEquals("第三方服务异常", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void testThrowBizException() {
        assertThrows(BizException.class, () -> {
            throw new BizException(CodeEnum.PARAM_ERROR);
        });
    }
}

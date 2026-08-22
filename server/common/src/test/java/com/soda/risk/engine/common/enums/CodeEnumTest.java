package com.soda.risk.engine.common.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CodeEnumTest {

    @Test
    void testSuccessCode() {
        assertEquals(0, CodeEnum.SUCCESS.getCode());
        assertEquals("成功", CodeEnum.SUCCESS.getMsg());
    }

    @Test
    void testSystemErrorCode() {
        assertEquals(-1, CodeEnum.SYSTEM_ERROR.getCode());
        assertEquals("系统异常", CodeEnum.SYSTEM_ERROR.getMsg());
    }

    @Test
    void testParamError() {
        assertEquals(1001, CodeEnum.PARAM_ERROR.getCode());
        assertEquals("参数错误", CodeEnum.PARAM_ERROR.getMsg());
    }

    @Test
    void testAuthFailed() {
        assertEquals(2001, CodeEnum.AUTH_FAILED.getCode());
    }

    @Test
    void testEngineComputeError() {
        assertEquals(4001, CodeEnum.ENGINE_COMPUTE_ERROR.getCode());
    }

    @Test
    void testThirdPartyError() {
        assertEquals(5001, CodeEnum.THIRD_PARTY_ERROR.getCode());
    }

    @Test
    void testEnumValues() {
        CodeEnum[] values = CodeEnum.values();
        assertTrue(values.length > 10, "Should have at least 10 enum values");
    }
}

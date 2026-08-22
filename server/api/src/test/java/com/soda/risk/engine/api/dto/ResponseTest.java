package com.soda.risk.engine.api.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void testSuccessWithData() {
        Response<String> response = Response.success("test data");
        assertEquals(0, response.getCode());
        assertEquals("test data", response.getData());
        assertEquals("成功", response.getMsg());
        assertTrue(response.isSuccess());
    }

    @Test
    void testSuccessWithoutData() {
        Response<Void> response = Response.success();
        assertEquals(0, response.getCode());
        assertNull(response.getData());
    }

    @Test
    void testFailWithCodeAndMsg() {
        Response<Object> response = Response.fail(-1, "系统错误");
        assertEquals(-1, response.getCode());
        assertEquals("系统错误", response.getMsg());
        assertFalse(response.isSuccess());
    }

    @Test
    void testIsSuccess() {
        Response<String> successResponse = Response.success("ok");
        assertTrue(successResponse.isSuccess());

        Response<Object> failResponse = Response.fail(-1, "error");
        assertFalse(failResponse.isSuccess());
    }
}

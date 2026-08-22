package com.soda.risk.engine.common.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommonUtils单元测试
 */
class CommonUtilsTest {

    @Test
    void testGenerateId() {
        String id1 = CommonUtils.generateId();
        String id2 = CommonUtils.generateId();
        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
        assertEquals(32, id1.length());
    }

    @Test
    void testIsEmpty() {
        assertTrue(CommonUtils.isEmpty(null));
        assertTrue(CommonUtils.isEmpty(""));
        assertTrue(CommonUtils.isEmpty("  "));
        assertFalse(CommonUtils.isEmpty("test"));
    }

    @Test
    void testObjectToMap() {
        Map<String, Object> original = new HashMap<>();
        original.put("key1", "value1");
        original.put("key2", 123);

        String json = CommonUtils.toJson(original);
        assertNotNull(json);

        Map<String, Object> result = CommonUtils.jsonToMap(json);
        assertEquals("value1", result.get("key1"));
        assertEquals(123, result.get("key2"));
    }

    @Test
    void testJsonToMap() {
        Map<String, Object> result = CommonUtils.jsonToMap("{\"key\":\"value\"}");
        assertEquals("value", result.get("key"));
    }

    @Test
    void testJsonToMapNull() {
        Map<String, Object> result = CommonUtils.jsonToMap(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testEquals() {
        assertTrue(CommonUtils.equals("test", "test"));
        assertFalse(CommonUtils.equals("test", "other"));
        assertFalse(CommonUtils.equals(null, "test"));
        assertTrue(CommonUtils.equals(null, null));
    }
}

package com.soda.risk.engine.core.strategy.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RuleExpressionEvaluator单元测试
 */
class RuleExpressionEvaluatorTest {

    private RuleExpressionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new RuleExpressionEvaluator(Collections.emptyList());
    }

    @Test
    void testSimpleComparison() {
        Map<String, Object> data = new HashMap<>();
        data.put("age", 25);
        assertTrue(evaluator.evaluate("age > 18", data));
        assertFalse(evaluator.evaluate("age < 18", data));
    }

    @Test
    void testStringComparison() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "active");
        assertTrue(evaluator.evaluate("status == 'active'", data));
        assertFalse(evaluator.evaluate("status == 'inactive'", data));
    }

    @Test
    void testComplexExpression() {
        Map<String, Object> data = new HashMap<>();
        data.put("age", 25);
        data.put("score", 85);
        assertTrue(evaluator.evaluate("age > 18 && score > 80", data));
        assertFalse(evaluator.evaluate("age > 18 && score > 90", data));
    }

    @Test
    void testNullExpression() {
        Map<String, Object> data = new HashMap<>();
        assertFalse(evaluator.evaluate(null, data));
        assertFalse(evaluator.evaluate("", data));
    }

    @Test
    void testInvalidExpression() {
        Map<String, Object> data = new HashMap<>();
        assertFalse(evaluator.evaluate("invalid expression !!!", data));
    }

    @Test
    void testEvaluateValue() {
        Map<String, Object> data = new HashMap<>();
        data.put("a", 10);
        data.put("b", 20);
        Object result = evaluator.evaluateValue("a + b", data);
        assertEquals(30L, result);
    }

    @Test
    void testClearCache() {
        Map<String, Object> data = new HashMap<>();
        data.put("x", 5);
        evaluator.evaluate("x > 0", data);
        evaluator.clearCache();
        // 应该仍然能正常工作
        assertTrue(evaluator.evaluate("x > 0", data));
    }
}

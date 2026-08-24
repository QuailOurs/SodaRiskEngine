package com.soda.risk.engine.core.strategy.engine.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.soda.risk.engine.core.strategy.rule.RuleExpressionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CustomExpressionFunctionsTest {

    private RuleExpressionEvaluator evaluator;

    @BeforeEach
    void setUp() throws Exception {
        List<AbstractFunction> functions = List.of(
                new IncludeOperatorFunction(),
                new NotIncludeOperatorFunction(),
                new IntersectionOperatorFunction(),
                new StringCompareFunctions.EqualsStringOperatorFunction(),
                new StringCompareFunctions.NotEqualsStringOperatorFunction(),
                new StringCompareFunctions.EqualsNullObjectOperatorFunction(),
                new StringCompareFunctions.StartsWithOperatorFunction(),
                new StringCompareFunctions.EndWithOperatorFunction(),
                new FieldCompareFunctions.FieldEqualsOperatorFunction(),
                new FieldCompareFunctions.FieldNotEqualsOperatorFunction(),
                new FieldCompareFunctions.FieldExistsOperatorFunction(),
                new FieldCompareFunctions.FieldNotExistsOperatorFunction(),
                new FieldCompareFunctions.FieldIncludeOperatorFunction(),
                new FieldCompareFunctions.FieldNotIncludeOperatorFunction(),
                new LengthAndIndexFunctions.LengthMoreThanAndOperatorFunction(),
                new LengthAndIndexFunctions.LengthLessThanAndOperatorFunction(),
                new LengthAndIndexFunctions.IndexMergeMoreThanOperatorFunction(),
                new LengthAndIndexFunctions.IndexMergeLessThanOperatorFunction(),
                new LengthAndIndexFunctions.IndexMergeEqualOperatorFunction(),
                new LengthAndIndexFunctions.IndexMergeMoreEqualOperatorFunction(),
                new LengthAndIndexFunctions.IndexMergeLessEqualOperatorFunction());
        evaluator = new RuleExpressionEvaluator(functions);
        evaluator.afterPropertiesSet();
    }

    @Test
    void validatesStringContainmentPrefixSuffixAndNullOperators() {
        Map<String, Object> data = new HashMap<>();
        data.put("text", "soda-risk-engine");
        data.put("nullable", null);

        assertThat(evaluator.evaluate("INCLUDE(text,'none,risk')", data)).isTrue();
        assertThat(evaluator.evaluate("NOT_INCLUDE(text,'none,other')", data)).isTrue();
        assertThat(evaluator.evaluate("STARTS_WITH(text,'soda,other')", data)).isTrue();
        assertThat(evaluator.evaluate("END_WITH(text,'engine,service')", data)).isTrue();
        assertThat(evaluator.evaluate("EQUAL_STRING(text,'soda-risk-engine')", data)).isTrue();
        assertThat(evaluator.evaluate("NOT_EQUAL_STRING(text,'other')", data)).isTrue();
        assertThat(evaluator.evaluate("EQUAL_NULL_OBJECT(nullable)", data)).isTrue();
        assertThat(evaluator.evaluate("INCLUDE(text,'')", data)).isFalse();
    }

    @Test
    void validatesCollectionsObjectArraysPrimitiveArraysAndNullIntersection() {
        Map<String, Object> data = new HashMap<>();
        data.put("tags", List.of("safe", "risk"));
        data.put("array", new String[]{"one", "two"});
        data.put("numbers", new int[]{1, 2, 3});
        data.put("empty", null);

        assertThat(evaluator.evaluate("INTERSECTION(tags,'none,risk')", data)).isTrue();
        assertThat(evaluator.evaluate("INTERSECTION(array,'two')", data)).isTrue();
        assertThat(evaluator.evaluate("INTERSECTION(numbers,'2,9')", data)).isTrue();
        assertThat(evaluator.evaluate("INTERSECTION(empty,'2')", data)).isFalse();
    }

    @Test
    void validatesFieldAndLengthOperators() {
        Map<String, Object> data = new HashMap<>();
        data.put("left", "risk-engine");
        data.put("right", "risk-engine");
        data.put("needles", "none,engine");

        assertThat(evaluator.evaluate("FIELD_EQUAL('left','right')", data)).isTrue();
        assertThat(evaluator.evaluate("FIELD_NOT_EQUAL('left','missing')", data)).isTrue();
        assertThat(evaluator.evaluate("FIELD_EXISTS('left')", data)).isTrue();
        assertThat(evaluator.evaluate("FIELD_NOT_EXISTS('missing')", data)).isTrue();
        assertThat(evaluator.evaluate("FIELD_INCLUDE('left','needles')", data)).isTrue();
        assertThat(evaluator.evaluate("FIELD_NOT_INCLUDE('left','missing')", data)).isTrue();
        assertThat(evaluator.evaluate("LENGTH_MORE_THAN_AND(left,11)", data)).isTrue();
        assertThat(evaluator.evaluate("LENGTH_LESS_THAN_AND(left,11)", data)).isTrue();
    }

    @Test
    void validatesAllMergedIndexComparisonsWithNumericAndInvalidValues() {
        Map<String, Object> data = Map.of("a", 4, "b", "6", "invalid", "not-a-number");

        assertThat(evaluator.evaluate("INDEX_MERGE_MORE_THAN('a,b,invalid','9')", data)).isTrue();
        assertThat(evaluator.evaluate("INDEX_MERGE_LESS_THAN('a,b','11')", data)).isTrue();
        assertThat(evaluator.evaluate("INDEX_MERGE_EQUAL('a,b','10')", data)).isTrue();
        assertThat(evaluator.evaluate("INDEX_MERGE_MORE_EQUAL('a,b','10')", data)).isTrue();
        assertThat(evaluator.evaluate("INDEX_MERGE_LESS_EQUAL('a,b','10')", data)).isTrue();
    }

    @Test
    void buildsExecutableExpressionsForEverySpecialOperatorShape() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "soda");
        data.put("nullable", null);
        data.put("other", "soda");

        assertThat(evaluateBuilt("name", "EQUAL_STRING", "soda", data)).isTrue();
        assertThat(evaluateBuilt("name", "LENGTH_MORE_THAN_AND", "4", data)).isTrue();
        assertThat(evaluateBuilt("nullable", "EQUAL_NULL_OBJECT", "ignored", data)).isTrue();
        assertThat(evaluateBuilt("name", "FIELD_EQUAL", "other", data)).isTrue();
        assertThat(evaluateBuilt("name", "FIELD_EXISTS", "ignored", data)).isTrue();
        assertThat(ExpressionOperatorTypeEnum.createExpression("INCLUDE", "name", "od"))
                .isEqualTo("INCLUDE(name,'od')");
    }

    private boolean evaluateBuilt(String field, String operator, String value, Map<String, Object> data) {
        String expression = ExpressionOperatorTypeEnum.buildRuleExpression(field, operator, value);
        return evaluator.evaluate(expression, data);
    }
}

package com.soda.risk.engine.core.strategy.rule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.api.dto.RuleHitResult;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RuleServiceTest {

    private RedisCacheService cache;
    private RuleService service;

    @BeforeEach
    void setUp() {
        cache = new RedisCacheService(new ObjectMapper(), null);
        service = new RuleService(cache, new RuleExpressionEvaluator(List.of()));
    }

    @Test
    void evaluatesExpressionListThresholdUnknownAndCorruptRuleIdsIndependently() {
        putRule(1, "score-rule", "EXPRESSION", Map.of("expression", "score >= 80"));
        putRule(2, "list-rule", "LIST", Map.of("checkField", "userId", "listKey", "blacklist"));
        putRule(3, "threshold-rule", "THRESHOLD",
                Map.of("checkField", "amount", "operator", ">", "threshold", "100"));
        putRule(4, "unknown-rule", "CUSTOM", Map.of());
        cache.sAdd(RedisKeyConstants.BLACK_WHITE_LIST + "blacklist", "blocked-user");
        cache.sAdd(RedisKeyConstants.SCENE_PREFIX + "checkout:rules", "1", "2", "3", "4", "broken");

        List<RuleHitResult> results = service.evaluateRules(
                Map.of("score", 90, "userId", "blocked-user", "amount", 120), "checkout");

        assertThat(results).hasSize(5);
        assertThat(results).filteredOn(RuleHitResult::isHit).extracting(RuleHitResult::getRuleId)
                .containsExactlyInAnyOrder(1L, 2L, 3L);
        assertThat(results).anySatisfy(result -> {
            assertThat(result.getRuleId()).isEqualTo(4L);
            assertThat(result.getDetail()).contains("未知规则类型");
        });
        assertThat(results).anySatisfy(result -> assertThat(result.getDetail()).contains("规则ID无效"));
    }

    @Test
    void returnsExplainableMissesForMissingConfigurationAndInvalidThresholdData() {
        assertThat(service.evaluateSingleRule(Map.of(), 99L))
                .satisfies(result -> {
                    assertThat(result.isHit()).isFalse();
                    assertThat(result.getDetail()).isEqualTo("规则配置不存在");
                });

        putRule(5, "threshold-rule", "THRESHOLD",
                Map.of("checkField", "amount", "operator", ">=", "threshold", "100"));
        RuleHitResult result = service.evaluateSingleRule(Map.of("amount", "not-a-number"), 5L);
        assertThat(result.isHit()).isFalse();
        assertThat(result.getParamValues()).containsEntry("amount", "not-a-number");
    }

    private void putRule(long id, String name, String type, Map<String, String> fields) {
        String key = RedisKeyConstants.RULE_PREFIX + id;
        cache.hSet(key, "name", name);
        cache.hSet(key, "key", "rule-" + id);
        cache.hSet(key, "type", type);
        fields.forEach((field, value) -> cache.hSet(key, field, value));
    }
}

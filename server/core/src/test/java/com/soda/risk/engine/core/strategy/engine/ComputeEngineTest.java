package com.soda.risk.engine.core.strategy.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.api.dto.RuleHitResult;
import com.soda.risk.engine.api.dto.StrategyHitResult;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.core.strategy.feature.FeatureService;
import com.soda.risk.engine.core.strategy.rule.RuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ComputeEngineTest {

    private FeatureService featureService;
    private RuleService ruleService;
    private RedisCacheService cache;
    private ComputeEngineImpl engine;

    @BeforeEach
    void setUp() {
        featureService = mock(FeatureService.class);
        ruleService = mock(RuleService.class);
        cache = mock(RedisCacheService.class);
        engine = new ComputeEngineImpl(new ObjectMapper(), featureService, ruleService, cache);
    }

    @Test
    void preprocessesJsonComplementsDeviceAndIpAndNormalizesNumericText() {
        Map<String, Object> result = engine.pretreatment(
                "{\"score\":\"12.5\",\"ip\":\"192.0.2.1\",\"deviceId\":\"device-1\"}",
                "login", "demo");

        assertThat(result).containsEntry("score", 12.5)
                .containsEntry("ipProvince", "")
                .containsEntry("ipCity", "")
                .containsEntry("deviceRisk", "NORMAL");
        assertThat(result.get("traceId").toString()).hasSize(32);
        assertThat(result).containsKeys("serverIp", "processTime");
    }

    @Test
    void executesFeatureAndRulePipelineAndReturnsExplainableHit() {
        when(featureService.queryAllFeatures(anyMap(), eq("login"))).thenReturn(Map.of("riskScore", 92));
        RuleHitResult hitRule = RuleHitResult.builder().ruleId(7L).hit(true)
                .paramValues(Map.of("amount", 200)).build();
        when(ruleService.evaluateRules(anyMap(), eq("login"))).thenReturn(List.of(hitRule));
        when(cache.get(RedisKeyConstants.SCENE_ROUTE_MAP + "login")).thenReturn("3");
        when(cache.get(RedisKeyConstants.STRATEGY_PREFIX + "3:name")).thenReturn("登录策略");

        StrategyHitResult result = engine.execute("{\"userId\":\"user-1\",\"amount\":\"200\"}",
                "login", "demo-business");

        assertThat(result.isHit()).isTrue();
        assertThat(result.getStrategyId()).isEqualTo(3L);
        assertThat(result.getStrategyName()).isEqualTo("登录策略");
        assertThat(result.getUserId()).isEqualTo("user-1");
        assertThat(result.getExtra()).containsEntry("hitRuleCount", 1)
                .containsEntry("totalRuleCount", 1);
        Map<?, ?> snapshot = (Map<?, ?>) result.getExtra().get("paramSnapshot");
        assertThat(snapshot.get("amount")).isEqualTo(200);

        ArgumentCaptor<Map<String, Object>> data = ArgumentCaptor.forClass(Map.class);
        verify(ruleService).evaluateRules(data.capture(), eq("login"));
        assertThat(data.getValue()).containsEntry("riskScore", 92).containsEntry("amount", 200.0);
    }

    @Test
    void returnsMissWithoutCallingDependenciesForEmptyOrInvalidJson() {
        assertThat(engine.execute("", "login", "demo").isHit()).isFalse();
        assertThat(engine.execute("not-json", "login", "demo").isHit()).isFalse();
        verifyNoInteractions(featureService, ruleService);
    }
}

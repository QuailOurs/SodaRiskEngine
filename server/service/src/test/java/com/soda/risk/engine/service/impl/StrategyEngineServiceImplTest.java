package com.soda.risk.engine.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.api.dto.*;
import com.soda.risk.engine.common.enums.CodeEnum;
import com.soda.risk.engine.service.engine.EngineEvaluationException;
import com.soda.risk.engine.service.engine.StrategyRuntimeEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StrategyEngineServiceImplTest {

    private StrategyRuntimeEngine runtime;
    private StrategyEngineServiceImpl service;

    @BeforeEach
    void setUp() {
        runtime = mock(StrategyRuntimeEngine.class);
        service = new StrategyEngineServiceImpl(runtime, new ObjectMapper());
    }

    @Test
    void evaluatesSingleAndBatchRequestsAndStopsOnBusinessErrors() {
        when(runtime.evaluate(any())).thenReturn(decision("r-1", true), decision("r-2", false));
        EngineEvaluateRequest first = request("r-1");
        EngineEvaluateRequest second = request("r-2");

        assertThat(service.evaluate(first).getData().isHit()).isTrue();
        Response<List<EngineDecisionResult>> batch = service.evaluateBatch(
                EngineBatchRequest.builder().requests(List.of(second)).build());
        assertThat(batch.isSuccess()).isTrue();
        assertThat(batch.getData()).extracting(EngineDecisionResult::getRequestId).containsExactly("r-2");

        when(runtime.evaluate(any())).thenThrow(new EngineEvaluationException(CodeEnum.AUTH_INVALID_KEY, "invalid"));
        assertThat(service.evaluate(first).getCode()).isEqualTo(CodeEnum.AUTH_INVALID_KEY.getCode());
        assertThat(service.evaluateBatch(EngineBatchRequest.builder().requests(List.of()).build()).getCode())
                .isEqualTo(CodeEnum.PARAM_NULL.getCode());
    }

    @Test
    void supportsLegacySingleAndBatchJsonContracts() {
        StrategyMatchResult match = StrategyMatchResult.builder().strategyId(9L).strategyName("策略")
                .strategyKey("S-9").rules(List.of(RuleHitResult.builder().ruleId(1L).hit(true).build())).build();
        EngineDecisionResult decision = decision("legacy", true);
        decision.setStrategies(List.of(match));
        when(runtime.evaluate(any())).thenReturn(decision);

        Response<StrategyHitResult> computed = service.compute("{\"score\":90}", "login", "demo");
        assertThat(computed.isSuccess()).isTrue();
        assertThat(computed.getData().getStrategyId()).isEqualTo(9L);
        assertThat(computed.getData().isHit()).isTrue();

        Response<String> batch = service.computeBatch("[{\"sceneKey\":\"login\",\"data\":{}}]", "demo");
        assertThat(batch.isSuccess()).isTrue();
        assertThat(batch.getData()).contains("legacy");
        assertThat(service.compute("not-json", "login", "demo").getCode())
                .isEqualTo(CodeEnum.DATA_PARSING_FAILED.getCode());
    }

    private EngineEvaluateRequest request(String id) {
        return EngineEvaluateRequest.builder().requestId(id).businessKey("demo")
                .sceneKey("login").data(Map.of()).build();
    }

    private EngineDecisionResult decision(String id, boolean hit) {
        return EngineDecisionResult.builder().requestId(id).traceId("trace-" + id).hit(hit)
                .status(hit ? "HIT" : "NOT_HIT").score(BigDecimal.TEN)
                .returnCodes(List.of()).strategies(List.of()).preStrategies(List.of()).build();
    }
}

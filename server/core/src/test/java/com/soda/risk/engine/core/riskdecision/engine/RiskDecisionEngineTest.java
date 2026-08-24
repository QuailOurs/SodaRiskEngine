package com.soda.risk.engine.core.riskdecision.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.api.dto.Response;
import com.soda.risk.engine.api.dto.RiskDecisionResult;
import com.soda.risk.engine.core.riskdecision.service.DecisionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RiskDecisionEngineTest {

    private DecisionService decisionService;
    private RiskDecisionEngine engine;

    @BeforeEach
    void setUp() {
        decisionService = mock(DecisionService.class);
        engine = new RiskDecisionEngine(decisionService, new ObjectMapper());
        when(decisionService.decideWithStrategy(anyMap(), anyString(), anyString()))
                .thenAnswer(invocation -> RiskDecisionResult.builder()
                        .score(80).riskLevel("HIGH").originalData(invocation.getArgument(0)).build());
        when(decisionService.decide(anyMap(), anyString(), anyString()))
                .thenReturn(RiskDecisionResult.builder().score(50).riskLevel("LOW").build());
    }

    @Test
    void routesCamelCaseBusinessTypesToTheirConfiguredScenes() {
        Response<RiskDecisionResult> login = engine.execute("{\"userId\":\"u-1\"}", "demo", "loginProtection");
        Response<RiskDecisionResult> account = engine.execute("{\"userId\":\"u-2\"}", "demo", "accountSecurity");

        assertThat(login.isSuccess()).isTrue();
        assertThat(login.getData().getTraceId()).hasSize(32);
        assertThat(login.getData().getOriginalData()).containsKeys("serverIp", "processTime", "traceId");
        verify(decisionService).decideWithStrategy(anyMap(), eq("demo"), eq("login_protection"));
        verify(decisionService).decideWithStrategy(anyMap(), eq("demo"), eq("account_security"));
    }

    @Test
    void fallsBackForCustomTypesAndRejectsInvalidJson() {
        Response<RiskDecisionResult> custom = engine.execute("{\"value\":1}", "demo", "customType");
        assertThat(custom.isSuccess()).isTrue();
        verify(decisionService).decide(anyMap(), eq("demo"), eq("customType"));

        assertThat(engine.execute("", "demo", "customType").isSuccess()).isFalse();
        assertThat(engine.execute("not-json", "demo", "customType").isSuccess()).isFalse();
    }
}

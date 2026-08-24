package com.soda.risk.engine.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LogStorageServiceTest {

    private ObjectMapper objectMapper;
    private LogStorageService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new LogStorageService(objectMapper, null, null);
    }

    @Test
    void storesAndQueriesEveryLogTypeInTheDevelopmentFallback() throws Exception {
        Map<String, Object> strategy = mutable("strategy-trace");
        Map<String, Object> disposer = mutable("disposer-trace");
        Map<String, Object> risk = mutable("risk-trace");

        service.storeStrategyHitLog(strategy);
        service.storeDisposerLog(disposer);
        service.storeRiskDecisionLog(risk);

        assertLogType("strategy-trace", "STRATEGY_HIT");
        assertLogType("disposer-trace", "DISPOSER");
        assertLogType("risk-trace", "RISK_DECISION");
    }

    @Test
    void generatesTraceIdWhenCallerDoesNotProvideOne() {
        Map<String, Object> log = new HashMap<>();
        service.storeDisposerLog(log);
        String traceId = log.get("traceId").toString();
        assertThat(traceId).hasSize(32);
        assertThat(service.queryByTraceId(traceId)).contains("DISPOSER");
    }

    private Map<String, Object> mutable(String traceId) {
        Map<String, Object> data = new HashMap<>();
        data.put("traceId", traceId);
        return data;
    }

    private void assertLogType(String traceId, String expected) throws Exception {
        JsonNode log = objectMapper.readTree(service.queryByTraceId(traceId));
        assertThat(log.path("logType").asText()).isEqualTo(expected);
        assertThat(log.path("timestamp").asText()).isNotBlank();
    }
}

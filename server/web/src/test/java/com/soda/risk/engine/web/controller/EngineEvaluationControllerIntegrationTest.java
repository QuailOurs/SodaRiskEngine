package com.soda.risk.engine.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.web.config.SodaApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SodaApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class EngineEvaluationControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void loadsConfigurationAndReturnsHitDecision() throws Exception {
        mockMvc.perform(get("/api/v1/engine/config/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.sceneCount").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.data.strategyCount").value(org.hamcrest.Matchers.greaterThan(0)));

        String response = mockMvc.perform(post("/api/v1/engine/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId":"integration-hit",
                                  "businessKey":"demo_business",
                                  "sceneKey":"login_protection",
                                  "needDetail":true,
                                  "data":{"blacklisted":true,"login_count":1,"device_risk_score":0}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("HIT"))
                .andExpect(jsonPath("$.data.hit").value(true))
                .andExpect(jsonPath("$.data.returnCodes[0]").value("VERIFY"))
                .andReturn().getResponse().getContentAsString();

        JsonNode body = objectMapper.readTree(response);
        assertThat(body.at("/data/strategies/0/rules").size()).isGreaterThan(0);
        assertThat(body.at("/data/configVersion").asLong()).isPositive();
    }

    @Test
    void returnsNotHitAndRejectsWrongBusinessKey() throws Exception {
        mockMvc.perform(post("/api/v1/engine/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"demo_business","sceneKey":"login_protection",
                                 "data":{"blacklisted":false,"login_count":1,"device_risk_score":0}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("NOT_HIT"));

        mockMvc.perform(post("/api/v1/engine/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"payment_business","sceneKey":"login_protection",
                                 "data":{"blacklisted":true}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2003));
    }

    @Test
    void evaluatesBatchAndReloadsConfigurationAtomically() throws Exception {
        String before = mockMvc.perform(get("/api/v1/engine/config/status"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long oldVersion = objectMapper.readTree(before).at("/data/version").asLong();

        mockMvc.perform(post("/api/v1/engine/evaluate/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"requests":[
                                  {"requestId":"batch-hit","businessKey":"demo_business",
                                   "sceneKey":"login_protection","data":{"blacklisted":true}},
                                  {"requestId":"batch-pass","businessKey":"demo_business",
                                   "sceneKey":"login_protection","data":{"blacklisted":false}}
                                ]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].status").value("HIT"))
                .andExpect(jsonPath("$.data[1].status").value("NOT_HIT"));

        String reloaded = mockMvc.perform(post("/api/v1/engine/config/reload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        long newVersion = objectMapper.readTree(reloaded).at("/data/version").asLong();
        assertThat(newVersion).isGreaterThan(oldVersion);
    }

    @Test
    void exposesSceneBusinessKeyForTheDebugConsole() throws Exception {
        mockMvc.perform(get("/api/strategy-engine-config-center/scene/sceneName/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].sceneKey").value("login_protection"))
                .andExpect(jsonPath("$.data[0].businessKey").value("demo_business"));
    }
}

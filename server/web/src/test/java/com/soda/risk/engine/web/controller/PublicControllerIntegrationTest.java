package com.soda.risk.engine.web.controller;

import com.soda.risk.engine.web.config.SodaApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SodaApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PublicControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void exposesConsistentHealthVersionAndStrategyHealthContracts() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.version").value("2.0.0-SNAPSHOT"));
        mockMvc.perform(get("/api/v1/health/version"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("soda"))
                .andExpect(jsonPath("$.data.version").value("2.0.0-SNAPSHOT"));
        mockMvc.perform(get("/api/v1/strategy/health"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value("OK"));
    }

    @Test
    void validatesDevelopmentLoginAndUserInformation() throws Exception {
        String token = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"admin\",\"password\":\"admin\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();
        String value = new com.fasterxml.jackson.databind.ObjectMapper().readTree(token).path("token").asText();
        mockMvc.perform(get("/get_info").param("token", value))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("admin"));
        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void executesQueriesAndReleasesDisposerStateThroughHttp() throws Exception {
        mockMvc.perform(post("/api/v1/disposer/execute")
                        .param("userId", "http-user").param("strategyId", "strategy-1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disposerType\":\"LOCK\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").value("http-user"));
        mockMvc.perform(get("/api/v1/disposer/status/http-user"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.locked").value(true));
        mockMvc.perform(post("/api/v1/disposer/release")
                        .param("userId", "http-user").param("disposerType", "LOCK"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/api/v1/disposer/status/http-user"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.locked").value(false));
    }

    @Test
    void evaluatesRiskIdentificationAndAccountSecurityRoutes() throws Exception {
        mockMvc.perform(post("/api/v1/risk/identification")
                        .param("openKey", "demo_business")
                        .param("data", "{\"userId\":\"normal-user\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.businessType").value("account_security"));
        mockMvc.perform(post("/api/v1/risk/account-security")
                        .param("openKey", "demo_business")
                        .param("data", "{\"userId\":\"normal-user\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.businessType").value("account_security"));
    }
}

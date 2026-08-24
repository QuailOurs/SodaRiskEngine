package com.soda.risk.engine.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.web.config.SodaApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SodaApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class ConfigurationManagementIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RedisCacheService cache;

    @Test
    void readsEveryConfigurationDomainFromTheSampleDataset() throws Exception {
        assertListContains("/api/strategy-engine-config-center/businessside/list", "{}", "businessSideKey", "demo_business");
        assertListContains("/api/strategy-engine-config-center/scene/list", "{}", "sceneKey", "login_protection");
        assertListContains("/api/strategy-engine-config-center/parameter/list", "{}", "paramKey", "user_id");
        assertListContains("/api/strategy-engine-config-center/tool/list", "{}", "name", "请求参数");
        assertListContains("/api/strategy-engine-config-center/rule/list", "{}", "ruleKey", "ip_blacklist");
        assertListContains("/api/strategy-engine-config-center/strategy/list", "{}", "strategyKey", "LOGIN_ABNORMAL");
        assertListContains("/api/feature-operation-center/feature/list", "{}", "featureKey", "ip_address");
        assertListContains("/api/strategy-engine-config-center/featureStatistics/list", "{}", "name", "十分钟登录次数");
        assertListContains("/api/strategy-engine-config-center/complement/list", "{}", "complementKey", "ip_region");
        assertListContains("/api/risk-decision-config-center/risk/list", "{}", "riskKey", "login_risk");
        assertListContains("/api/risk-decision-config-center/blackWhiteList/list", "{}", "listValue", "192.0.2.100");
        assertListContains("/api/risk-decision-config-center/returnCode/list", "{}", "returnCode", "VERIFY");
        assertListContains("/api/disposer-config-center/disposerConfig/list", "{}", "disposerKey", "lock_account");
    }

    @Test
    void createsUpdatesRelatesAndDeletesBusinessSceneRuleStrategyAndFeatureData() throws Exception {
        long businessId = postForId("/api/strategy-engine-config-center/businessside/add", """
                {"name":"测试业务方","businessSideKey":"test_business","systemKey":"test-system"}
                """);
        mockMvc.perform(get("/api/strategy-engine-config-center/businessside/existed/key/test_business"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value("existed"));
        putJson("/api/strategy-engine-config-center/businessside/update", """
                {"id":%d,"name":"测试业务方-已更新","businessSideKey":"test_business","systemKey":"test-system"}
                """.formatted(businessId));

        long sceneId = postForId("/api/strategy-engine-config-center/scene/add", """
                {"name":"测试场景","sceneKey":"test_scene","businessSideId":%d,
                 "businessSideKey":"test_business","state":1}
                """.formatted(businessId));
        mockMvc.perform(get("/api/strategy-engine-config-center/scene/existed")
                        .param("businessSide", "test_business").param("name", "测试场景"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));

        long ruleId = postForId("/api/strategy-engine-config-center/rule/add", """
                {"name":"测试阈值规则","ruleKey":"test_rule","sceneKey":"test_scene",
                 "ruleType":"EXPRESSION","type":0,"expression":"amount > 100","state":1}
                """);
        mockMvc.perform(get("/api/strategy-engine-config-center/rule/validExist/test_scene/测试阈值规则"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));

        long strategyId = postForId("/api/strategy-engine-config-center/strategy/add", """
                {"name":"测试策略","strategyKey":"TEST_STRATEGY","sceneKey":"test_scene",
                 "type":1,"strategyType":1,"priority":1,"threshold":1,"score":80,
                 "returnCode":"VERIFY","state":1,"ruleIds":[%d]}
                """.formatted(ruleId));
        mockMvc.perform(get("/api/strategy-engine-config-center/strategy/id/{id}", strategyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ruleIds[0]").value(ruleId))
                .andExpect(jsonPath("$.data.rules[0].ruleKey").value("test_rule"));
        postJson("/api/strategy-engine-config-center/strategy/update/state",
                "{\"id\":" + strategyId + ",\"state\":2}");
        mockMvc.perform(get("/api/strategy-engine-config-center/strategy/sceneKey/test_scene"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(strategyId));
        postJson("/api/strategy-engine-config-center/strategy/update/state",
                "{\"id\":" + strategyId + ",\"state\":0}");
        assertThat(cache.get(RedisKeyConstants.SCENE_ROUTE_MAP + "test_scene")).isNull();
        assertThat(cache.get(RedisKeyConstants.STRATEGY_PREFIX + strategyId)).isNull();
        postJson("/api/strategy-engine-config-center/strategy/update/state",
                "{\"id\":" + strategyId + ",\"state\":2}");

        long featureId = postForId("/api/feature-operation-center/feature/add", """
                {"name":"测试特征","featureKey":"test_feature","featureType":"base",
                 "dataType":"STRING","sceneKey":"test_scene","state":1}
                """);
        postJson("/api/feature-operation-center/feature/update/state",
                "{\"id\":" + featureId + ",\"state\":0}");
        mockMvc.perform(get("/api/feature-operation-center/feature/validExist/test_feature"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));

        deleteOk("/api/feature-operation-center/feature/" + featureId);
        deleteOk("/api/strategy-engine-config-center/strategy/" + strategyId);
        assertThat(cache.get(RedisKeyConstants.SCENE_ROUTE_MAP + "test_scene")).isNull();
        assertThat(cache.get(RedisKeyConstants.STRATEGY_PREFIX + strategyId)).isNull();
        deleteOk("/api/strategy-engine-config-center/rule/delete/" + ruleId);
        deleteOk("/api/strategy-engine-config-center/scene/" + sceneId);
        deleteOk("/api/strategy-engine-config-center/businessside/" + businessId);
    }

    @Test
    void createsUpdatesAndDeletesRiskListReturnCodeAndDisposerData() throws Exception {
        postJson("/api/risk-decision-config-center/risk/add", """
                {"name":"测试风险配置","riskKey":"test_risk","businessType":"TEST",
                 "riskLevel":2,"scoreThreshold":70,"disposition":"alert_notify","state":1}
                """);
        JsonNode risks = postResponse("/api/risk-decision-config-center/risk/list", "{\"riskKey\":\"test_risk\"}");
        long riskId = risks.at("/data/0/id").asLong();
        putJson("/api/risk-decision-config-center/risk/update", """
                {"id":%d,"name":"测试风险配置-更新","riskKey":"test_risk","businessType":"TEST",
                 "riskLevel":3,"scoreThreshold":90,"disposition":"ban_account","state":1}
                """.formatted(riskId));
        deleteOk("/api/risk-decision-config-center/risk/" + riskId);

        postJson("/api/risk-decision-config-center/blackWhiteList/add", """
                {"listType":"BLACK","listKey":"userId","listValue":"test-user","state":1}
                """);
        JsonNode lists = postResponse("/api/risk-decision-config-center/blackWhiteList/list",
                "{\"listValue\":\"test-user\"}");
        long listId = lists.at("/data/0/id").asLong();
        postJson("/api/risk-decision-config-center/blackWhiteList/update/state",
                "{\"id\":" + listId + ",\"state\":0}");
        deleteOk("/api/risk-decision-config-center/blackWhiteList/" + listId);

        postJson("/api/risk-decision-config-center/returnCode/add", """
                {"returnCode":"TEST_CODE","name":"测试返回码","sceneKey":"login_protection","state":1}
                """);
        JsonNode codes = postResponse("/api/risk-decision-config-center/returnCode/list",
                "{\"returnCode\":\"TEST_CODE\"}");
        long codeId = codes.at("/data/0/id").asLong();
        putJson("/api/risk-decision-config-center/returnCode/update", """
                {"id":%d,"returnCode":"TEST_CODE","name":"测试返回码-更新",
                 "sceneKey":"login_protection","state":1}
                """.formatted(codeId));
        deleteOk("/api/risk-decision-config-center/returnCode/" + codeId);

        long disposerId = postForId("/api/disposer-config-center/disposerConfig", """
                {"name":"测试处置","disposerType":"ALERT","disposerKey":"test_alert","state":1}
                """);
        putJson("/api/disposer-config-center/disposerConfig/status",
                "{\"id\":" + disposerId + ",\"state\":0}");
        deleteOk("/api/disposer-config-center/disposerConfig/" + disposerId);
    }

    @Test
    void managesCatalogParametersStatisticsComplementsToolsAndExpressionValidation() throws Exception {
        long parameterId = postForScalarId("/api/strategy-engine-config-center/parameter/add", """
                {"name":"测试参数","paramKey":"test_param","typeId":"5",
                 "sceneKey":"login_protection","description":"测试"}
                """);
        mockMvc.perform(get("/api/strategy-engine-config-center/parameter/existedKey")
                        .param("sceneKey", "login_protection").param("paramKey", "test_param"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));
        putJson("/api/strategy-engine-config-center/parameter/update", """
                {"id":%d,"name":"测试参数-更新","typeId":"5","description":"已更新"}
                """.formatted(parameterId));

        long statisticId = postForScalarId("/api/strategy-engine-config-center/featureStatistics/add", """
                {"name":"测试统计特征","sceneKey":"login_protection","featureId":1,"valId":1,
                 "beforeMinute":5,"writeState":"启用","state":"启用","identificationParas":[1]}
                """);
        mockMvc.perform(get("/api/strategy-engine-config-center/featureStatistics/{id}", statisticId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.identificationParas[0]").value(1));
        patchOk("/api/strategy-engine-config-center/featureStatistics/forbidden/" + statisticId);
        patchOk("/api/strategy-engine-config-center/featureStatistics/enable/" + statisticId);

        long complementKeyId = postForScalarId("/api/strategy-engine-config-center/complement/addComplementKey", """
                {"toolId":6,"complementKey":"test_complement","description":"测试补全"}
                """);
        long relationId = postForScalarId("/api/strategy-engine-config-center/complement/add", """
                {"paramId":1,"complementKeyId":%d}
                """.formatted(complementKeyId));
        patchOk("/api/strategy-engine-config-center/complement/forbidden/" + relationId);
        patchOk("/api/strategy-engine-config-center/complement/enable/" + relationId);

        patchOk("/api/strategy-engine-config-center/tool/forbidden/1");
        patchOk("/api/strategy-engine-config-center/tool/enable/1");
        mockMvc.perform(post("/api/strategy-engine-config-center/checkExpression/checkRegExp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"testData\":\"abc-123\",\"searchField\":\"id\",\"regExp\":\"[a-z]+-[0-9]+\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.matched").value(true));
        mockMvc.perform(post("/api/strategy-engine-config-center/checkExpression/checkRegExp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"testData\":\"abc\",\"regExp\":\"[\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.valid").value(false));

        deleteOk("/api/strategy-engine-config-center/featureStatistics/" + statisticId);
        deleteOk("/api/strategy-engine-config-center/parameter/delete/" + parameterId);
    }

    @Test
    void synchronizesEverySupportedConfigurationGroupIncludingOnlineStrategies() throws Exception {
        for (String type : new String[]{"strategy", "rule", "scene", "feature", "disposer", "risk", "all"}) {
            mockMvc.perform(post("/api/v1/config/sync/" + type))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }
        assertThat(cache.get(RedisKeyConstants.SCENE_ROUTE_MAP + "login_protection")).isEqualTo("1");
        assertThat(cache.sMembers(RedisKeyConstants.SCENE_PREFIX + "login_protection:rules"))
                .contains("1", "2");
        mockMvc.perform(post("/api/v1/strategy/compute")
                        .param("data", "{\"blacklisted\":true}")
                        .param("sceneKey", "login_protection")
                        .param("openKey", "demo_business"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.hit").value(true));
    }

    @Test
    void returnsOrderedStructuredReportForTagReleaseSynchronization() throws Exception {
        mockMvc.perform(post("/api/v1/config/sync/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.skipped").value(false))
                .andExpect(jsonPath("$.data.steps.length()").value(7))
                .andExpect(jsonPath("$.data.steps[0].domain").value("scene"))
                .andExpect(jsonPath("$.data.steps[1].domain").value("feature"))
                .andExpect(jsonPath("$.data.steps[2].domain").value("rule"))
                .andExpect(jsonPath("$.data.steps[3].domain").value("strategy"))
                .andExpect(jsonPath("$.data.steps[6].domain").value("black-white"));

        mockMvc.perform(post("/api/v1/config/sync/risk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.steps.length()").value(2))
                .andExpect(jsonPath("$.data.steps[0].domain").value("risk"))
                .andExpect(jsonPath("$.data.steps[1].domain").value("black-white"));
    }

    private void assertListContains(String path, String request, String field, String value) throws Exception {
        JsonNode body = postResponse(path, request);
        JsonNode data = body.path("data");
        if (data.has("records")) data = data.path("records");
        assertThat(data.isArray()).as(path).isTrue();
        boolean found = false;
        for (JsonNode row : data) if (value.equals(row.path(field).asText())) found = true;
        assertThat(found).as(path + " should contain " + field + "=" + value).isTrue();
    }

    private JsonNode postResponse(String path, String body) throws Exception {
        String response = mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private long postForId(String path, String body) throws Exception {
        JsonNode response = postResponse(path, body);
        assertThat(response.path("code").asInt()).isEqualTo(200);
        long id = response.at("/data/id").asLong();
        assertThat(id).isPositive();
        return id;
    }

    private long postForScalarId(String path, String body) throws Exception {
        JsonNode response = postResponse(path, body);
        long id = response.path("data").asLong();
        assertThat(id).isPositive();
        return id;
    }

    private void postJson(String path, String body) throws Exception {
        mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }

    private void putJson(String path, String body) throws Exception {
        mockMvc.perform(put(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }

    private void patchOk(String path) throws Exception {
        mockMvc.perform(patch(path)).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }

    private void deleteOk(String path) throws Exception {
        mockMvc.perform(delete(path)).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }
}

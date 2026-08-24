package com.soda.risk.engine.core.riskdecision.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.api.dto.RiskDecisionResult;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionServiceTest {

    private RedisCacheService cache;
    private DecisionService service;

    @BeforeEach
    void setUp() {
        cache = new RedisCacheService(new ObjectMapper(), null);
        service = new DecisionService(cache);
    }

    @Test
    void appliesBlacklistWhitelistAndNeutralRiskScores() {
        cache.sAdd(RedisKeyConstants.BLACK_WHITE_LIST + "blacklist", "blocked-user", "192.0.2.9");
        cache.sAdd(RedisKeyConstants.BLACK_WHITE_LIST + "whitelist", "trusted-user");

        RiskDecisionResult blocked = service.decide(Map.of("userId", "blocked-user"), "demo", "LOGIN");
        RiskDecisionResult trusted = service.decide(Map.of("userId", "trusted-user"), "demo", "LOGIN");
        RiskDecisionResult neutral = service.decide(Map.of("userId", "new-user"), "demo", "LOGIN");

        assertThat(blocked.getScore()).isEqualTo(100);
        assertThat(blocked.getRiskLevel()).isEqualTo("HIGH");
        assertThat(trusted.getScore()).isZero();
        assertThat(trusted.getRiskLevel()).isEqualTo("SAFE");
        assertThat(neutral.getScore()).isEqualTo(50);
        assertThat(neutral.getRiskLevel()).isEqualTo("LOW");
    }

    @Test
    void includesConfiguredRiskThresholdsInDecisionDetails() {
        cache.hSet(RedisKeyConstants.RISK_PREFIX + "LOGIN", "login-risk", "80");
        RiskDecisionResult result = service.decideWithStrategy(Map.of("userId", "user-1"), "demo", "LOGIN");

        assertThat(result.getDetail()).containsKey("riskConfig");
        Map<?, ?> riskConfig = (Map<?, ?>) result.getDetail().get("riskConfig");
        assertThat(riskConfig.get("login-risk")).isEqualTo("80");
    }
}

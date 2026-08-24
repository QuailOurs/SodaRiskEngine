package com.soda.risk.engine.service.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.common.enums.CodeEnum;
import com.soda.risk.engine.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticationServiceTest {

    private RedisCacheService cache;
    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        cache = new RedisCacheService(new ObjectMapper(), null);
        service = new AuthenticationService(cache);
    }

    @Test
    void authenticatesSceneKeysExplicitMappingsAndBusinessSideHashes() {
        assertThat(service.auth("login_protection")).isEqualTo("login_protection");

        cache.set(RedisKeyConstants.BUSINESS_SIDE_KEY + "token-1", "demo-business");
        assertThat(service.auth("token-1")).isEqualTo("demo-business");

        cache.hSet(RedisKeyConstants.BUSINESS_SIDE_OPENID + "business-2", "name", "Business 2");
        assertThat(service.auth("business-2")).isEqualTo("business-2");
    }

    @Test
    void rejectsBlankAndUnknownKeysInsteadOfTreatingAnEmptyHashAsARecord() {
        assertThatThrownBy(() -> service.auth(" "))
                .isInstanceOfSatisfying(BizException.class,
                        error -> assertThat(error.getCode()).isEqualTo(CodeEnum.AUTH_FAILED.getCode()));
        assertThat(service.findOpenId("missing")).isNull();
        assertThatThrownBy(() -> service.auth("missing"))
                .isInstanceOfSatisfying(BizException.class,
                        error -> assertThat(error.getCode()).isEqualTo(CodeEnum.AUTH_INVALID_KEY.getCode()));
    }
}

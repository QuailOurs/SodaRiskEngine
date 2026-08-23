package com.soda.risk.engine.service.auth;

import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {
    private RedisCacheService cache;
    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        cache = mock(RedisCacheService.class);
        service = new AuthenticationService(cache);
    }

    @Test
    void rejectsBlankKey() {
        assertThrows(BizException.class, () -> service.auth("  "));
        verifyNoInteractions(cache);
    }

    @Test
    void resolvesMappedOpenId() {
        when(cache.get(RedisKeyConstants.BUSINESS_SIDE_KEY + "secret")).thenReturn("merchant-1");
        assertEquals("merchant-1", service.auth("secret"));
    }

    @Test
    void acceptsExistingOpenId() {
        when(cache.hGetAll(RedisKeyConstants.BUSINESS_SIDE_OPENID + "merchant"))
                .thenReturn(Map.of("name", "demo"));
        assertEquals("merchant", service.auth("merchant"));
    }

    @Test
    void rejectsUnknownKey() {
        assertThrows(BizException.class, () -> service.auth("unknown"));
    }

    @Test
    void preservesDocumentedSceneKeyCompatibility() {
        assertEquals("merchant_login", service.auth("merchant_login"));
        verifyNoInteractions(cache);
    }
}

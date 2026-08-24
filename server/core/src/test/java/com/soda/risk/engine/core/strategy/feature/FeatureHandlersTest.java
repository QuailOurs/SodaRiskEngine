package com.soda.risk.engine.core.strategy.feature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.core.thirdparty.ThirdPartyServiceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeatureHandlersTest {

    private RedisCacheService cache;

    @BeforeEach
    void setUp() {
        cache = new RedisCacheService(new ObjectMapper(), null);
    }

    @Test
    void readsBaseCalculationStatisticsListAuthenticationAndPortraitFeatures() {
        BaseFeatureHandler base = new BaseFeatureHandler();
        assertThat(base.queryFeatures(Map.of("ip", "192.0.2.1"), configs("requestIp", "ip")))
                .containsEntry("requestIp", "192.0.2.1");

        cache.set(RedisKeyConstants.FEATURE_CALCULATION + "rolling-score", "12.5");
        CalculationFeatureHandler calculation = new CalculationFeatureHandler(cache);
        assertThat(calculation.supports("statistics")).isFalse();
        assertThat(calculation.queryFeatures(Map.of(), configs("score", "rolling-score")))
                .containsEntry("score", 12.5);

        cache.set(RedisKeyConstants.FEATURE_CALCULATION + "count:10m:user-1", "8");
        StatisticsFeatureHandler statistics = new StatisticsFeatureHandler(cache);
        assertThat(statistics.queryFeatures(Map.of("userId", "user-1"),
                configs("loginCount", "count:10m:userId"))).containsEntry("loginCount", 8.0);

        cache.sAdd(RedisKeyConstants.BLACK_WHITE_LIST + "ip", "192.0.2.1");
        ListFeatureHandler list = new ListFeatureHandler(cache);
        assertThat(list.querySingle(Map.of("config", "ip", "ip", "192.0.2.1"))).isEqualTo(1);

        cache.set(RedisKeyConstants.FEATURE_AUTH + "realname:user-1", "1");
        AuthFeatureHandler auth = new AuthFeatureHandler(cache);
        assertThat(auth.querySingle(Map.of("config", "realname", "userId", "user-1"))).isEqualTo(1);

        cache.set(RedisKeyConstants.FEATURE_PORTRAIT + "level:user-1", "gold");
        PortraitFeatureHandler portrait = new PortraitFeatureHandler(cache);
        assertThat(portrait.querySingle(Map.of("config", "level:user-1"))).isEqualTo("gold");
    }

    @Test
    void delegatesAlgorithmFeaturesAndReturnsZeroForMalformedConfiguration() {
        ThirdPartyServiceFactory factory = mock(ThirdPartyServiceFactory.class);
        when(factory.query(eq("algorithm"), org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(Map.of("score", 91.5));
        AlgorithmFeatureHandler handler = new AlgorithmFeatureHandler(factory);

        assertThat(handler.querySingle(Map.of("config", "device-risk:deviceId", "deviceId", "d-1")))
                .isEqualTo(91.5);
        assertThat(handler.querySingle(Map.of("config", "malformed"))).isEqualTo(0.0);
    }

    private List<Map.Entry<Object, Object>> configs(Object key, Object value) {
        return List.of(new AbstractMap.SimpleEntry<>(key, value));
    }
}

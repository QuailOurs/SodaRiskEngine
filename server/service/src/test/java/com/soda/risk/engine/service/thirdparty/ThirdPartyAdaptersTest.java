package com.soda.risk.engine.service.thirdparty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.common.cache.RedisCacheService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ThirdPartyAdaptersTest {

    @Test
    void validatesDeterministicStubAdapterContracts() {
        BaiduIpScoreServiceAdapter baidu = new BaiduIpScoreServiceAdapter();
        assertThat(baidu.supports("baidu_ip")).isTrue();
        assertThat(baidu.query(Map.of("ip", "192.0.2.1")))
                .containsEntry("ip", "192.0.2.1").containsEntry("score", 0);

        ShumeiServiceAdapter shumei = new ShumeiServiceAdapter();
        assertThat(shumei.supports("shumei_phone")).isTrue();
        assertThat(shumei.query(Map.of("phone", "13800000000")))
                .containsEntry("phone", "13800000000").containsEntry("riskLevel", 0);

        TencentRiskServiceAdapter tencent = new TencentRiskServiceAdapter();
        assertThat(tencent.supports("tencent_risk")).isTrue();
        assertThat(tencent.query(Map.of("userId", "u-1"))).containsEntry("score", 0);

        UmcServiceAdapter umc = new UmcServiceAdapter();
        assertThat(umc.supports("user")).isTrue();
        assertThat(umc.query(Map.of("userId", "u-1")))
                .containsEntry("userId", "u-1").containsEntry("exists", false);

        HunterServiceAdapter hunter = new HunterServiceAdapter();
        assertThat(hunter.supports("hunter")).isTrue();
        assertThat(hunter.query(Map.of("dataId", "event-1")))
                .containsEntry("dataId", "event-1").containsEntry("processed", false);
    }

    @Test
    void readsPortraitFieldsFromTheCacheAdapter() {
        RedisCacheService cache = new RedisCacheService(new ObjectMapper(), null);
        cache.hSet("soda:portrait:security:user-1", "level", "high");
        PortraitServiceAdapter adapter = new PortraitServiceAdapter(cache);

        assertThat(adapter.supports("portrait")).isTrue();
        assertThat(adapter.query(Map.of("userId", "user-1", "queryType", "security")))
                .containsEntry("level", "high");
        assertThat(adapter.query(Map.of())).isEmpty();
    }
}

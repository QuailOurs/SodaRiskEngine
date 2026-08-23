package com.soda.risk.engine.service.thirdparty;

import com.soda.risk.engine.core.thirdparty.ThirdPartyServiceAdapter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UnavailableThirdPartyAdaptersTest {
    static Stream<Object[]> adapters() {
        return Stream.of(
                new Object[]{new TencentRiskServiceAdapter(), "tencent", Map.of("phone", "13800000000")},
                new Object[]{new ShumeiServiceAdapter(), "shumei", Map.of("phone", "13800000000")},
                new Object[]{new BaiduIpScoreServiceAdapter(), "baidu_ip", Map.of("ip", "192.0.2.1")},
                new Object[]{new HunterServiceAdapter(), "hunter", Map.of("dataId", "event-1")},
                new Object[]{new UmcServiceAdapter(), "user", Map.of("userId", "user-1")}
        );
    }

    @ParameterizedTest
    @MethodSource("adapters")
    void unavailableIntegrationsNeverPretendToBeSafe(ThirdPartyServiceAdapter adapter,
                                                      String type, Map<String, Object> params) {
        assertTrue(adapter.supports(type));
        Map<String, Object> result = adapter.query(params);
        assertEquals("NOT_CONFIGURED", result.get("status"));
        assertEquals(false, result.get("available"));
        assertFalse(result.containsKey("riskLevel"));
        assertFalse(result.containsKey("score"));
    }
}

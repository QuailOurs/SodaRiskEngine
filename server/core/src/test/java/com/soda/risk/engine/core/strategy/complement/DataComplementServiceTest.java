package com.soda.risk.engine.core.strategy.complement;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DataComplementServiceTest {

    @Test
    void composesHandlersWithoutMutatingOriginalData() {
        Map<String, Object> original = new LinkedHashMap<>(Map.of("ip", "192.0.2.1", "deviceId", "d-1"));
        DataComplementService service = new DataComplementService(List.of(
                new IpDataComplementHandler(), new DeviceDataComplementHandler()));

        DataComplementResult result = service.complete("login", original);

        assertThat(result.data()).containsEntry("ipProvince", "")
                .containsEntry("ipCity", "")
                .containsEntry("deviceRisk", "NORMAL");
        assertThat(result.degraded()).isFalse();
        assertThat(original).containsOnlyKeys("ip", "deviceId");
    }

    @Test
    void isolatesHandlerFailureAndContinuesThePipeline() {
        DataComplementHandler failed = new DataComplementHandler() {
            public String name() { return "failed-source"; }
            public boolean supports(String sceneKey, Map<String, Object> data) { return true; }
            public Map<String, Object> complement(String sceneKey, Map<String, Object> data) {
                throw new IllegalStateException("unavailable");
            }
        };
        DataComplementHandler fallback = new DataComplementHandler() {
            public String name() { return "fallback"; }
            public boolean supports(String sceneKey, Map<String, Object> data) { return true; }
            public Map<String, Object> complement(String sceneKey, Map<String, Object> data) {
                return Map.of("completed", true);
            }
        };

        DataComplementResult result = new DataComplementService(List.of(failed, fallback))
                .complete("login", Map.of("userId", "u-1"));

        assertThat(result.data()).containsEntry("completed", true);
        assertThat(result.failedHandlers()).containsExactly("failed-source");
        assertThat(result.degraded()).isTrue();
    }
}

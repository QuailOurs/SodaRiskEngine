package com.soda.risk.engine.core.strategy.complement;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.LinkedHashMap;

/** 设备数据补全的默认适配器；真实设备指纹服务可按相同接口接入。 */
@Component
public class DeviceDataComplementHandler implements DataComplementHandler {

    @Override
    public String name() {
        return "device";
    }

    @Override
    public boolean supports(String sceneKey, Map<String, Object> data) {
        return data.get("deviceId") != null;
    }

    @Override
    public Map<String, Object> complement(String sceneKey, Map<String, Object> data) {
        Map<String, Object> result = new LinkedHashMap<>();
        Object current = data.get("deviceRisk");
        result.put("deviceRisk", current == null ? "NORMAL" : current);
        return result;
    }
}

package com.soda.risk.engine.core.strategy.complement;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/** IP 数据补全的默认适配器；可由接入真实 IP 服务的实现替换或扩展。 */
@Component
public class IpDataComplementHandler implements DataComplementHandler {

    @Override
    public String name() {
        return "ip";
    }

    @Override
    public boolean supports(String sceneKey, Map<String, Object> data) {
        Object ip = data.containsKey("ip") ? data.get("ip") : data.get("userIp");
        return ip != null && !String.valueOf(ip).isBlank();
    }

    @Override
    public Map<String, Object> complement(String sceneKey, Map<String, Object> data) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ipProvince", data.getOrDefault("ipProvince", ""));
        result.put("ipCity", data.getOrDefault("ipCity", ""));
        result.put("ipIsp", data.getOrDefault("ipIsp", ""));
        return result;
    }
}

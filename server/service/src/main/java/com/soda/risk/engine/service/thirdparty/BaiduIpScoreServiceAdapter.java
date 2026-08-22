package com.soda.risk.engine.service.thirdparty;

import com.soda.risk.engine.core.thirdparty.ThirdPartyServiceAdapter;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * IP 评分服务适配器。
 * 当前为存根实现，后续接入实际百度API
 */
@Slf4j
@Component
public class BaiduIpScoreServiceAdapter implements ThirdPartyServiceAdapter {

    @Override
    public boolean supports(String serviceType) {
        return "baidu".equalsIgnoreCase(serviceType) || "baidu_ip".equalsIgnoreCase(serviceType);
    }

    @Override
    public Map<String, Object> query(Map<String, Object> params) {
        long start = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            String ip = (String) params.get("ip");

            // TODO: 调用百度IP评分API
            result.put("service", "baidu_ip_score");
            result.put("ip", ip);
            result.put("score", 0);
            result.put("riskLevel", "safe");

        } catch (Exception e) {
            log.error("Baidu IP score query failed", e);
            result.put("error", e.getMessage());
        }

        MonitorFacade.insert("[thirdparty]Baidu", System.currentTimeMillis() - start);
        return result;
    }

    @Override
    public String getServiceName() {
        return "BaiduIpScoreService";
    }
}

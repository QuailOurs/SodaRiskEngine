package com.soda.risk.engine.service.thirdparty;

import com.soda.risk.engine.core.thirdparty.ThirdPartyServiceAdapter;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用用户管理服务适配器。
 * 当前为存根实现，后续可接入实际用户服务
 */
@Slf4j
@Component
public class UmcServiceAdapter implements ThirdPartyServiceAdapter {

    @Override
    public boolean supports(String serviceType) {
        return "umc".equalsIgnoreCase(serviceType) || "user".equalsIgnoreCase(serviceType);
    }

    @Override
    public Map<String, Object> query(Map<String, Object> params) {
        long start = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            String userId = (String) params.get("userId");
            if (userId == null) return result;

            result.put("userId", userId);
            result.put("status", "NOT_CONFIGURED");
            result.put("available", false);

        } catch (Exception e) {
            log.error("UMC query failed", e);
        }

        MonitorFacade.insert("[thirdparty]UMC", System.currentTimeMillis() - start);
        return result;
    }

    @Override
    public String getServiceName() {
        return "UMCService";
    }
}

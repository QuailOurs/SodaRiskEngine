package com.soda.risk.engine.service.thirdparty;

import com.soda.risk.engine.core.thirdparty.ThirdPartyServiceAdapter;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 第三方设备风险服务适配器。
 * 当前为存根实现，后续接入实际数美API
 */
@Slf4j
@Component
public class ShumeiServiceAdapter implements ThirdPartyServiceAdapter {

    @Override
    public boolean supports(String serviceType) {
        return "shumei".equalsIgnoreCase(serviceType) || "shumei_phone".equalsIgnoreCase(serviceType);
    }

    @Override
    public Map<String, Object> query(Map<String, Object> params) {
        long start = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            String phone = (String) params.get("phone");

            result.put("service", "shumei_phone_risk");
            result.put("phone", phone);
            result.put("status", "NOT_CONFIGURED");
            result.put("available", false);

        } catch (Exception e) {
            log.error("Shumei query failed", e);
            result.put("error", e.getMessage());
        }

        MonitorFacade.insert("[thirdparty]Shumei", System.currentTimeMillis() - start);
        return result;
    }

    @Override
    public String getServiceName() {
        return "ShumeiService";
    }
}

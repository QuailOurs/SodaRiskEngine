package com.soda.risk.engine.service.thirdparty;

import com.soda.risk.engine.core.thirdparty.ThirdPartyServiceAdapter;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import com.soda.risk.engine.common.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 第三方风险识别服务适配器。
 * 当前为存根实现，后续接入实际腾讯天御API
 */
@Slf4j
@Component
public class TencentRiskServiceAdapter implements ThirdPartyServiceAdapter {

    @Override
    public boolean supports(String serviceType) {
        return "tencent".equalsIgnoreCase(serviceType) || "tencent_risk".equalsIgnoreCase(serviceType);
    }

    @Override
    public Map<String, Object> query(Map<String, Object> params) {
        long start = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            String phone = (String) params.get("phone");
            String ip = (String) params.get("ip");
            String userId = (String) params.get("userId");

            result.put("service", "tencent_risk");
            result.put("status", "NOT_CONFIGURED");
            result.put("available", false);

        } catch (Exception e) {
            log.error("Tencent risk query failed", e);
            result.put("error", e.getMessage());
        }

        MonitorFacade.insert("[thirdparty]Tencent", System.currentTimeMillis() - start);
        return result;
    }

    @Override
    public String getServiceName() {
        return "TencentRiskService";
    }
}

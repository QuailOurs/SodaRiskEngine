package com.soda.risk.engine.service.thirdparty;

import com.soda.risk.engine.core.thirdparty.ThirdPartyServiceAdapter;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用事件处理中心服务适配器。
 * 当前为存根实现，后续可接入实际猎户服务
 */
@Slf4j
@Component
public class HunterServiceAdapter implements ThirdPartyServiceAdapter {

    @Override
    public boolean supports(String serviceType) {
        return "hunter".equalsIgnoreCase(serviceType);
    }

    @Override
    public Map<String, Object> query(Map<String, Object> params) {
        long start = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            String dataId = (String) params.get("dataId");
            if (dataId == null) return result;

            result.put("dataId", dataId);
            result.put("status", "NOT_CONFIGURED");
            result.put("available", false);

        } catch (Exception e) {
            log.error("Hunter query failed", e);
        }

        MonitorFacade.insert("[thirdparty]Hunter", System.currentTimeMillis() - start);
        return result;
    }

    @Override
    public String getServiceName() {
        return "HunterService";
    }
}

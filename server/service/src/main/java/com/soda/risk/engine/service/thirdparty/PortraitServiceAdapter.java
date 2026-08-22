package com.soda.risk.engine.service.thirdparty;

import com.soda.risk.engine.core.thirdparty.ThirdPartyServiceAdapter;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户画像服务适配器。
 * 当前使用Redis缓存查询，后续可改为HTTP/gRPC调用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PortraitServiceAdapter implements ThirdPartyServiceAdapter {

    private final RedisCacheService redisCacheService;

    @Override
    public boolean supports(String serviceType) {
        return "portrait".equalsIgnoreCase(serviceType);
    }

    @Override
    public Map<String, Object> query(Map<String, Object> params) {
        long start = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            String userId = (String) params.get("userId");
            String queryType = (String) params.getOrDefault("queryType", "default");

            if (userId == null || userId.isEmpty()) {
                return result;
            }

            // 从Redis查询画像数据
            String portraitKey = "soda:portrait:" + queryType + ":" + userId;
            Map<Object, Object> portraitData = redisCacheService.hGetAll(portraitKey);

            if (portraitData != null && !portraitData.isEmpty()) {
                portraitData.forEach((k, v) -> result.put((String) k, v));
            }

        } catch (Exception e) {
            log.error("Portrait query failed", e);
        }

        MonitorFacade.insert("[thirdparty]Portrait", System.currentTimeMillis() - start);
        return result;
    }

    @Override
    public String getServiceName() {
        return "PortraitService";
    }
}

package com.soda.risk.engine.core.thirdparty;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 第三方服务工厂 - 统一管理第三方服务调用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThirdPartyServiceFactory {

    private final List<ThirdPartyServiceAdapter> adapters;

    /**
     * 查询第三方服务
     * @param serviceType 服务类型
     * @param params 查询参数
     * @return 查询结果
     */
    public Map<String, Object> query(String serviceType, Map<String, Object> params) {
        ThirdPartyServiceAdapter adapter = adapters.stream()
                .filter(a -> a.supports(serviceType))
                .findFirst()
                .orElse(null);

        if (adapter == null) {
            log.warn("No third-party service adapter found for type: {}", serviceType);
            return Map.of();
        }

        return adapter.query(params);
    }
}

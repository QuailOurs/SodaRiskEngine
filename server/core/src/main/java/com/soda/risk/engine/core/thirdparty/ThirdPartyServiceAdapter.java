package com.soda.risk.engine.core.thirdparty;

import java.util.Map;

/**
 * 第三方服务适配器接口
 * 统一抽象第三方服务调用，解耦对具体第三方的依赖
 */
public interface ThirdPartyServiceAdapter {

    /**
     * 是否支持该服务
     */
    boolean supports(String serviceType);

    /**
     * 查询第三方服务
     * @param params 查询参数
     * @return 查询结果
     */
    Map<String, Object> query(Map<String, Object> params);

    /**
     * 获取服务名称
     */
    String getServiceName();
}

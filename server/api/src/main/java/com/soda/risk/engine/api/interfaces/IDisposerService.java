package com.soda.risk.engine.api.interfaces;

import com.soda.risk.engine.api.dto.DisposerResponse;

import java.util.Map;

/**
 * 处置服务接口
 */
public interface IDisposerService {

    /**
     * 执行处置
     * @param userId 用户ID
     * @param strategyId 策略ID
     * @param params 附加参数
     * @return 处置响应
     */
    DisposerResponse execute(String userId, String strategyId, Map<String, Object> params);

    /**
     * 解除处置
     * @param userId 用户ID
     * @param disposerType 处置类型
     * @return 处置响应
     */
    DisposerResponse release(String userId, String disposerType);

    /**
     * 查询处置状态
     * @param userId 用户ID
     * @return 处置状态
     */
    Map<String, Object> queryStatus(String userId);
}

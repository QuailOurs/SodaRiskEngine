package com.soda.risk.engine.service.impl;

import com.soda.risk.engine.api.dto.DisposerResponse;
import com.soda.risk.engine.api.interfaces.IDisposerService;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import com.soda.risk.engine.core.disposer.flow.DisposerFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 处置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisposerServiceImpl implements IDisposerService {

    private final DisposerFlowService disposerFlowService;
    private final RedisCacheService redisCacheService;
    private final LogStorageService logStorageService;

    @Override
    public DisposerResponse execute(String userId, String strategyId, Map<String, Object> params) {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> result = disposerFlowService.dispose(userId, strategyId, params);

            boolean success = Boolean.TRUE.equals(result.get("success"));
            String message = (String) result.getOrDefault("message", "");

            // 存储处置日志
            Map<String, Object> logData = new HashMap<>();
            logData.put("userId", userId);
            logData.put("strategyId", strategyId);
            logData.put("success", success);
            logData.put("message", message);
            logData.put("params", params);
            logStorageService.storeDisposerLog(logData);

            MonitorFacade.insert("[service]Disposer", System.currentTimeMillis() - start);

            return DisposerResponse.builder()
                    .success(success)
                    .message(message)
                    .userId(userId)
                    .disposerType((String) result.get("disposerType"))
                    .build();

        } catch (Exception e) {
            log.error("Disposer execute failed, userId={}", userId, e);
            return DisposerResponse.builder()
                    .success(false)
                    .message("处置执行异常: " + e.getMessage())
                    .userId(userId)
                    .build();
        }
    }

    @Override
    public DisposerResponse release(String userId, String disposerType) {
        try {
            String key = RedisKeyConstants.DISPOSER_USER + userId + ":" + disposerType;
            redisCacheService.delete(key);

            return DisposerResponse.builder()
                    .success(true)
                    .message("处置已解除")
                    .userId(userId)
                    .disposerType(disposerType)
                    .build();

        } catch (Exception e) {
            log.error("Disposer release failed, userId={}", userId, e);
            return DisposerResponse.builder()
                    .success(false)
                    .message("解除处置异常: " + e.getMessage())
                    .userId(userId)
                    .build();
        }
    }

    @Override
    public Map<String, Object> queryStatus(String userId) {
        Map<String, Object> status = new HashMap<>();
        status.put("userId", userId);

        try {
            String lockKey = RedisKeyConstants.DISPOSER_USER + userId + ":LOCK";
            String banKey = RedisKeyConstants.DISPOSER_USER + userId + ":BAN";

            status.put("locked", redisCacheService.hasKey(lockKey));
            status.put("banned", redisCacheService.hasKey(banKey));

        } catch (Exception e) {
            log.error("Query disposer status failed, userId={}", userId, e);
            status.put("error", e.getMessage());
        }

        return status;
    }
}

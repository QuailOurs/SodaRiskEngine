package com.soda.risk.engine.core.disposer.flow;

import com.soda.risk.engine.api.dto.DisposerResponse;
import com.soda.risk.engine.api.dto.StrategyHitResult;
import com.soda.risk.engine.core.disposer.handler.AbstractDisposerWayHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处置流程服务
 * 统一管理处置流程，整合原始disposerEngine的三步流程：
 * 1. 处置执行 (DisposerService)
 * 2. 告警通知 (WarnService)
 * 3. 数据保存 (SaveDataService)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisposerFlowService {

    private final List<AbstractDisposerWayHandler> disposerHandlers;
    private final Map<String, AbstractDisposerWayHandler> handlerCache = new ConcurrentHashMap<>();

    /**
     * 执行处置流程（完整流程：处置 -> 告警 -> 保存）
     */
    public DisposerResponse execute(StrategyHitResult hitResult) {
        if (!hitResult.isHit()) {
            return DisposerResponse.builder()
                    .success(false)
                    .message("策略未命中，无需处置")
                    .build();
        }

        String traceId = hitResult.getTraceId() != null ? hitResult.getTraceId() : UUID.randomUUID().toString().replace("-", "");
        String userId = hitResult.getUserId() != null ? hitResult.getUserId() : "unknown";

        // 1. 执行处置
        String disposerType = getDisposerType(hitResult);
        DisposerResponse response = doDispose(hitResult, disposerType);

        // 2. 发送告警
        if (response.isSuccess()) {
            sendWarn(hitResult, response, traceId, userId);
        }

        // 3. 保存处置记录
        saveDisposerRecord(hitResult, response, traceId, userId);

        log.info("[{}] DisposerFlow completed, userId={}, type={}, success={}",
                traceId, userId, disposerType, response.isSuccess());
        return response;
    }

    /**
     * 执行处置 - 简化接口
     */
    public Map<String, Object> dispose(String userId, String strategyId, Map<String, Object> params) {
        Map<String, Object> result = new java.util.HashMap<>();
        try {
            String disposerType = (String) params.getOrDefault("disposerType", "ALERT");

            StrategyHitResult hitResult = StrategyHitResult.builder()
                    .hit(true)
                    .userId(userId)
                    .disposerResponse(DisposerResponse.builder().disposerType(disposerType).build())
                    .params(params)
                    .build();

            DisposerResponse response = execute(hitResult);
            result.put("success", response.isSuccess());
            result.put("message", response.getMessage());
            result.put("disposerType", disposerType);
        } catch (Exception e) {
            log.error("Dispose failed, userId={}, strategyId={}", userId, strategyId, e);
            result.put("success", false);
            result.put("message", "处置执行异常: " + e.getMessage());
        }
        return result;
    }

    /**
     * 批量处置
     */
    public List<DisposerResponse> executeBatch(List<StrategyHitResult> hitResults) {
        return hitResults.stream()
                .map(this::execute)
                .toList();
    }

    // ======================== 内部方法 ========================

    /**
     * 执行实际处置逻辑
     */
    private DisposerResponse doDispose(StrategyHitResult hitResult, String disposerType) {
        AbstractDisposerWayHandler handler = getHandler(disposerType);
        if (handler == null) {
            log.warn("No disposer handler found for type={}", disposerType);
            return DisposerResponse.builder()
                    .success(false)
                    .message("未找到处置方式: " + disposerType)
                    .build();
        }

        try {
            return handler.execute(hitResult);
        } catch (Exception e) {
            log.error("Disposer execute failed, type={}", disposerType, e);
            return DisposerResponse.builder()
                    .success(false)
                    .disposerType(disposerType)
                    .message("处置执行异常: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 发送告警
     */
    private void sendWarn(StrategyHitResult hitResult, DisposerResponse response,
                          String traceId, String userId) {
        try {
            String disposerType = response.getDisposerType();
            // 只有LOCK和BAN类型需要告警
            if (!"LOCK".equals(disposerType) && !"BAN".equals(disposerType)) {
                return;
            }

            String warnTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            log.warn("[WARN] {}-处置告警: userId={}, disposerType={}, strategyKey={}, sceneKey={}, hitRules={}",
                    traceId, userId, disposerType,
                    hitResult.getStrategyKey(), hitResult.getSceneKey(),
                    hitResult.getHitRules() != null ? hitResult.getHitRules().size() : 0);

            // TODO: 通过通知适配器接入邮件、短信或 Webhook
            // emailService.sendWarnEmail(...);
            // smsService.sendWarnSms(...);

        } catch (Exception e) {
            log.error("SendWarn failed, userId={}", userId, e);
        }
    }

    /**
     * 保存处置记录
     */
    private void saveDisposerRecord(StrategyHitResult hitResult, DisposerResponse response,
                                    String traceId, String userId) {
        try {
            String disposerType = response.getDisposerType();

            log.info("[SAVE] {}-处置记录: userId={}, disposerType={}, success={}, strategyKey={}, sceneKey={}",
                    traceId, userId, disposerType, response.isSuccess(),
                    hitResult.getStrategyKey(), hitResult.getSceneKey());

            // TODO: 通过处置记录仓储持久化执行结果
            // disposerInfoMapper.insert(new DisposerInfo(...));

            // TODO: 通过日志适配器写入检索或消息系统
            // logStorageEsImpl.store(logObj);
            // logStorageKafkaImpl.store(logObj);

        } catch (Exception e) {
            log.error("SaveDisposerRecord failed, userId={}", userId, e);
        }
    }

    private AbstractDisposerWayHandler getHandler(String disposerType) {
        return handlerCache.computeIfAbsent(disposerType, type ->
                disposerHandlers.stream()
                        .filter(h -> h.supports(type))
                        .findFirst()
                        .orElse(null)
        );
    }

    private String getDisposerType(StrategyHitResult hitResult) {
        if (hitResult.getDisposerResponse() != null && hitResult.getDisposerResponse().getDisposerType() != null) {
            return hitResult.getDisposerResponse().getDisposerType();
        }
        return "ALERT";
    }
}

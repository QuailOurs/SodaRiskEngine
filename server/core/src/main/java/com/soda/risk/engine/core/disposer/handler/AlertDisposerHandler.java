package com.soda.risk.engine.core.disposer.handler;

import com.soda.risk.engine.api.dto.DisposerResponse;
import com.soda.risk.engine.api.dto.StrategyHitResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 告警处置处理器 - 发送告警通知
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertDisposerHandler extends AbstractDisposerWayHandler {

    @Override
    protected DisposerResponse doDispose(StrategyHitResult hitResult) {
        try {
            // 发送告警通知（可通过Kafka/邮件/短信等方式）
            log.warn("ALERT: Strategy hit detected, strategyKey={}, sceneKey={}, traceId={}",
                    hitResult.getStrategyKey(), hitResult.getSceneKey(), hitResult.getTraceId());

            return DisposerResponse.builder()
                    .disposerName("告警通知")
                    .disposerType(getDisposerType())
                    .success(true)
                    .message("告警已发送")
                    .build();

        } catch (Exception e) {
            log.error("Alert failed", e);
            return DisposerResponse.builder()
                    .success(false)
                    .message("告警发送失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public String getDisposerType() {
        return "ALERT";
    }

    @Override
    public boolean supports(String disposerType) {
        return "ALERT".equalsIgnoreCase(disposerType) || "alert".equalsIgnoreCase(disposerType);
    }
}

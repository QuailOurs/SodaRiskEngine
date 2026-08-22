package com.soda.risk.engine.core.disposer.handler;

import com.soda.risk.engine.api.dto.DisposerResponse;
import com.soda.risk.engine.api.dto.StrategyHitResult;
import lombok.extern.slf4j.Slf4j;

/**
 * 处置方式处理器抽象类
 * 使用模板方法模式统一处置流程
 */
@Slf4j
public abstract class AbstractDisposerWayHandler {

    /**
     * 执行处置 - 模板方法
     */
    public DisposerResponse execute(StrategyHitResult hitResult) {
        long start = System.currentTimeMillis();
        try {
            // 1. 前置校验
            if (!preCheck(hitResult)) {
                return DisposerResponse.builder()
                        .success(false)
                        .message("前置校验不通过")
                        .build();
            }

            // 2. 执行处置
            DisposerResponse response = doDispose(hitResult);

            // 3. 后置处理
            afterDispose(hitResult, response);

            log.info("Disposer executed, type={}, userId={}, result={}, cost={}ms",
                    getDisposerType(), getUserId(hitResult), response.isSuccess(),
                    System.currentTimeMillis() - start);

            return response;

        } catch (Exception e) {
            log.error("Disposer execute failed, type={}", getDisposerType(), e);
            return DisposerResponse.builder()
                    .success(false)
                    .message("处置执行异常: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 前置校验
     */
    protected boolean preCheck(StrategyHitResult hitResult) {
        return hitResult != null && hitResult.isHit();
    }

    /**
     * 执行具体处置逻辑
     */
    protected abstract DisposerResponse doDispose(StrategyHitResult hitResult);

    /**
     * 后置处理 - 记录日志、发送通知等
     */
    protected void afterDispose(StrategyHitResult hitResult, DisposerResponse response) {
        // 默认空实现，子类可覆盖
    }

    /**
     * 获取处置方式类型
     */
    public abstract String getDisposerType();

    /**
     * 是否支持该处置方式
     */
    public abstract boolean supports(String disposerType);

    protected String getUserId(StrategyHitResult hitResult) {
        if (hitResult.getExtra() != null) {
            Object userId = hitResult.getExtra().get("userId");
            return userId != null ? userId.toString() : "unknown";
        }
        return "unknown";
    }
}

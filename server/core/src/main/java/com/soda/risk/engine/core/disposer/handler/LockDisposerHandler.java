package com.soda.risk.engine.core.disposer.handler;

import com.soda.risk.engine.api.dto.DisposerResponse;
import com.soda.risk.engine.api.dto.StrategyHitResult;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 锁定处置处理器
 * 整合了原始的每日限额检查、分布式锁、周期锁定逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LockDisposerHandler extends AbstractDisposerWayHandler {

    private final RedisCacheService redisCacheService;

    /** 每日锁定上限 */
    private static final int DEFAULT_DAILY_LIMIT = 200;
    /** 周期锁过期时间（15天 = 1296000秒） */
    private static final int CYCLE_LOCK_EXPIRE_SECONDS = 1296000;
    /** 分布式锁过期时间（30分钟） */
    private static final int DISTRIBUTED_LOCK_EXPIRE_SECONDS = 1800;

    @Override
    protected boolean preCheck(StrategyHitResult hitResult) {
        if (!super.preCheck(hitResult)) {
            return false;
        }

        String userId = getUserId(hitResult);
        try {
            // 1. 检查今日锁定数是否达到上限
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String limitKey = RedisKeyConstants.OFFLINE_LOCK + "limit:" + date;
            String todayCount = redisCacheService.get(limitKey);
            int lockCount = 0;
            if (StringUtils.isNumeric(todayCount)) {
                lockCount = Integer.parseInt(todayCount);
            }
            if (lockCount >= getDailyLimit()) {
                log.warn("Lock daily limit reached, count={}, userId={}", lockCount, userId);
                return false;
            }

            // 2. 获取分布式锁（防止并发重复处理）
            String distLockKey = RedisKeyConstants.OFFLINE_LOCK + "dist:" + userId;
            Boolean locked = redisCacheService.setIfAbsent(distLockKey, "1", DISTRIBUTED_LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(locked)) {
                log.warn("Lock distributed lock not acquired, userId={}", userId);
                return false;
            }

            // 3. 检查周期锁定（15天内已锁定则跳过）
            String cycleLockKey = RedisKeyConstants.OFFLINE_LOCK + "cycle:" + userId;
            Boolean cycleExists = redisCacheService.setIfAbsent(cycleLockKey, "1", CYCLE_LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(cycleExists)) {
                log.warn("User locked within cycle period, userId={}", userId);
                // 释放分布式锁
                redisCacheService.delete(distLockKey);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Lock preCheck failed, userId={}", userId, e);
            return false;
        }
    }

    @Override
    protected DisposerResponse doDispose(StrategyHitResult hitResult) {
        String userId = getUserId(hitResult);
        long durationMinutes = getDuration(hitResult);

        try {
            // 设置锁定状态到Redis
            String lockKey = RedisKeyConstants.DISPOSER_USER + userId + ":LOCK";
            redisCacheService.set(lockKey, "LOCKED", durationMinutes, TimeUnit.MINUTES);

            // 更新今日锁定计数
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String limitKey = RedisKeyConstants.OFFLINE_LOCK + "limit:" + date;
            redisCacheService.increment(limitKey);

            log.info("Account locked, userId={}, duration={}min", userId, durationMinutes);

            return DisposerResponse.builder()
                    .disposerName("账号锁定")
                    .disposerType(getDisposerType())
                    .success(true)
                    .userId(userId)
                    .message("账号已锁定，锁定时长: " + durationMinutes + "分钟")
                    .build();

        } catch (Exception e) {
            log.error("Lock account failed, userId={}", userId, e);
            return DisposerResponse.builder()
                    .success(false)
                    .message("锁定失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    protected void afterDispose(StrategyHitResult hitResult, DisposerResponse response) {
        // 释放分布式锁
        String userId = getUserId(hitResult);
        String distLockKey = RedisKeyConstants.OFFLINE_LOCK + "dist:" + userId;
        redisCacheService.delete(distLockKey);
    }

    @Override
    public String getDisposerType() {
        return "LOCK";
    }

    @Override
    public boolean supports(String disposerType) {
        return "LOCK".equalsIgnoreCase(disposerType);
    }

    private long getDuration(StrategyHitResult hitResult) {
        if (hitResult.getExtra() != null) {
            Object duration = hitResult.getExtra().get("lockDuration");
            if (duration != null) {
                try {
                    return Long.parseLong(duration.toString());
                } catch (NumberFormatException ignored) {}
            }
        }
        return 30; // 默认锁定30分钟
    }

    private int getDailyLimit() {
        return DEFAULT_DAILY_LIMIT;
    }
}

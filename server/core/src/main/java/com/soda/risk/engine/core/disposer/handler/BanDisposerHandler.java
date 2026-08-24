package com.soda.risk.engine.core.disposer.handler;

import com.soda.risk.engine.api.dto.DisposerResponse;
import com.soda.risk.engine.api.dto.StrategyHitResult;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 封禁处置处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BanDisposerHandler extends AbstractDisposerWayHandler {

    private final RedisCacheService redisCacheService;

    @Override
    protected DisposerResponse doDispose(StrategyHitResult hitResult) {
        String userId = getUserId(hitResult);
        long durationDays = getDuration(hitResult);

        try {
            String banKey = RedisKeyConstants.DISPOSER_USER + userId + ":BAN";
            redisCacheService.set(banKey, "BANNED", durationDays, TimeUnit.DAYS);

            log.info("Account banned, userId={}, duration={}days", userId, durationDays);

            return DisposerResponse.builder()
                    .disposerName("账号封禁")
                    .disposerType(getDisposerType())
                    .success(true)
                    .message("账号已封禁，封禁时长: " + durationDays + "天")
                    .build();

        } catch (Exception e) {
            log.error("Ban account failed, userId={}", userId, e);
            return DisposerResponse.builder()
                    .success(false)
                    .message("封禁失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public String getDisposerType() {
        return "BAN";
    }

    @Override
    public boolean supports(String disposerType) {
        return "BAN".equalsIgnoreCase(disposerType);
    }

    private long getDuration(StrategyHitResult hitResult) {
        if (hitResult.getExtra() != null) {
            Object duration = hitResult.getExtra().get("banDuration");
            if (duration != null) {
                try {
                    return Long.parseLong(duration.toString());
                } catch (NumberFormatException ignored) {}
            }
        }
        return 7; // 默认封禁7天
    }
}

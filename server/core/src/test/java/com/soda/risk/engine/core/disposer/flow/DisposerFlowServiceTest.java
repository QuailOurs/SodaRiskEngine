package com.soda.risk.engine.core.disposer.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.api.dto.DisposerResponse;
import com.soda.risk.engine.api.dto.StrategyHitResult;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.core.disposer.handler.AlertDisposerHandler;
import com.soda.risk.engine.core.disposer.handler.BanDisposerHandler;
import com.soda.risk.engine.core.disposer.handler.LockDisposerHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DisposerFlowServiceTest {

    private RedisCacheService cache;
    private DisposerFlowService service;

    @BeforeEach
    void setUp() {
        cache = new RedisCacheService(new ObjectMapper(), null);
        service = new DisposerFlowService(List.of(
                new LockDisposerHandler(cache), new BanDisposerHandler(cache), new AlertDisposerHandler()));
    }

    @Test
    void skipsMissesAndExecutesDefaultAlertForHits() {
        assertThat(service.execute(StrategyHitResult.builder().hit(false).build()))
                .satisfies(result -> {
                    assertThat(result.isSuccess()).isFalse();
                    assertThat(result.getMessage()).contains("无需处置");
                });

        DisposerResponse alert = service.execute(StrategyHitResult.builder()
                .hit(true).userId("user-1").traceId("trace-alert").build());
        assertThat(alert.isSuccess()).isTrue();
        assertThat(alert.getDisposerType()).isEqualTo("ALERT");
    }

    @Test
    void storesLockAndBanUnderTheStatusKeysUsedByThePublicService() {
        Map<String, Object> locked = service.dispose("user-lock", "strategy-1", Map.of("disposerType", "LOCK"));
        Map<String, Object> banned = service.dispose("user-ban", "strategy-2", Map.of("disposerType", "BAN"));

        assertThat(locked).containsEntry("success", true).containsEntry("disposerType", "LOCK");
        assertThat(banned).containsEntry("success", true).containsEntry("disposerType", "BAN");
        assertThat(cache.get(RedisKeyConstants.DISPOSER_USER + "user-lock:LOCK")).isEqualTo("LOCKED");
        assertThat(cache.get(RedisKeyConstants.DISPOSER_USER + "user-ban:BAN")).isEqualTo("BANNED");
        assertThat(cache.hasKey(RedisKeyConstants.OFFLINE_LOCK + "dist:user-lock")).isFalse();
    }

    @Test
    void rejectsUnknownHandlersAndDailyLockLimit() {
        DisposerResponse unknown = service.execute(hit("user-1", "UNKNOWN"));
        assertThat(unknown.isSuccess()).isFalse();
        assertThat(unknown.getMessage()).contains("未找到处置方式");

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        cache.set(RedisKeyConstants.OFFLINE_LOCK + "limit:" + date, "200");
        DisposerResponse limited = service.execute(hit("user-limited", "LOCK"));
        assertThat(limited.isSuccess()).isFalse();
        assertThat(limited.getMessage()).isEqualTo("前置校验不通过");
    }

    @Test
    void executesBatchWithoutDroppingIndividualResults() {
        List<DisposerResponse> results = service.executeBatch(List.of(
                StrategyHitResult.builder().hit(false).build(),
                hit("user-alert", "ALERT")));
        assertThat(results).hasSize(2);
        assertThat(results.get(0).isSuccess()).isFalse();
        assertThat(results.get(1).isSuccess()).isTrue();
    }

    private StrategyHitResult hit(String userId, String type) {
        return StrategyHitResult.builder().hit(true).userId(userId)
                .disposerResponse(DisposerResponse.builder().disposerType(type).build()).build();
    }
}

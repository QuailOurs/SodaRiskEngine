package com.soda.risk.engine.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.api.dto.DisposerResponse;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.core.disposer.flow.DisposerFlowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

class DisposerServiceImplTest {

    private RedisCacheService cache;
    private DisposerFlowService flow;
    private LogStorageService logs;
    private DisposerServiceImpl service;

    @BeforeEach
    void setUp() {
        cache = new RedisCacheService(new ObjectMapper(), null);
        flow = mock(DisposerFlowService.class);
        logs = mock(LogStorageService.class);
        service = new DisposerServiceImpl(flow, cache, logs);
    }

    @Test
    void executesAndRecordsDisposerResults() {
        when(flow.dispose("user-1", "strategy-1", Map.of("disposerType", "LOCK")))
                .thenReturn(Map.of("success", true, "message", "locked", "disposerType", "LOCK"));

        DisposerResponse response = service.execute("user-1", "strategy-1", Map.of("disposerType", "LOCK"));

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getUserId()).isEqualTo("user-1");
        assertThat(response.getDisposerType()).isEqualTo("LOCK");
        verify(logs).storeDisposerLog(argThat(log -> Boolean.TRUE.equals(log.get("success"))
                && "user-1".equals(log.get("userId"))));
    }

    @Test
    void queriesAndReleasesTheSameKeysWrittenByHandlers() {
        String lockKey = RedisKeyConstants.DISPOSER_USER + "user-2:LOCK";
        String banKey = RedisKeyConstants.DISPOSER_USER + "user-2:BAN";
        cache.set(lockKey, "LOCKED");
        cache.set(banKey, "BANNED");

        assertThat(service.queryStatus("user-2"))
                .containsEntry("locked", true).containsEntry("banned", true);
        assertThat(service.release("user-2", "LOCK").isSuccess()).isTrue();
        assertThat(cache.hasKey(lockKey)).isFalse();
        assertThat(cache.hasKey(banKey)).isTrue();
    }
}

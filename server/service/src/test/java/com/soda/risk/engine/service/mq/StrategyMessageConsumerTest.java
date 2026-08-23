package com.soda.risk.engine.service.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.api.dto.StrategyHitResult;
import com.soda.risk.engine.core.disposer.flow.DisposerFlowService;
import com.soda.risk.engine.core.strategy.engine.ComputeEngine;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class StrategyMessageConsumerTest {
    private final ComputeEngine engine = mock(ComputeEngine.class);
    private final DisposerFlowService disposer = mock(DisposerFlowService.class);
    private final Acknowledgment acknowledgment = mock(Acknowledgment.class);
    private final StrategyMessageConsumer consumer =
            new StrategyMessageConsumer(engine, disposer, new ObjectMapper());

    @Test
    void acknowledgesSuccessfulStrategyMessage() {
        StrategyHitResult result = StrategyHitResult.builder().hit(false).build();
        when(engine.execute("{}", "scene", "kafka")).thenReturn(result);
        consumer.onStrategyMessage(new ConsumerRecord<>("strategy", 0, 1, "scene", "{}"), acknowledgment);
        verify(acknowledgment).acknowledge();
        verifyNoInteractions(disposer);
    }

    @Test
    void doesNotAcknowledgeFailedStrategyMessage() {
        when(engine.execute(anyString(), anyString(), anyString())).thenThrow(new IllegalStateException("down"));
        assertThrows(StrategyMessageConsumer.MessageProcessingException.class,
                () -> consumer.onStrategyMessage(new ConsumerRecord<>("strategy", 0, 1, "scene", "{}"), acknowledgment));
        verifyNoInteractions(acknowledgment);
    }

    @Test
    void parsesAndExecutesDisposerMessageBeforeAcknowledging() throws Exception {
        String json = new ObjectMapper().writeValueAsString(StrategyHitResult.builder().hit(true).traceId("t1").build());
        consumer.onDisposerMessage(new ConsumerRecord<>("disposer", 0, 1, "key", json), acknowledgment);
        verify(disposer).execute(argThat(result -> result.isHit() && "t1".equals(result.getTraceId())));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void malformedDisposerMessageIsRetried() {
        assertThrows(StrategyMessageConsumer.MessageProcessingException.class,
                () -> consumer.onDisposerMessage(new ConsumerRecord<>("disposer", 0, 1, "key", "not-json"), acknowledgment));
        verifyNoInteractions(acknowledgment, disposer);
    }
}

package com.soda.risk.engine.service.mq;

import com.soda.risk.engine.api.dto.Response;
import com.soda.risk.engine.api.dto.RiskDecisionResult;
import com.soda.risk.engine.api.dto.StrategyHitResult;
import com.soda.risk.engine.core.disposer.flow.DisposerFlowService;
import com.soda.risk.engine.core.riskdecision.engine.RiskDecisionEngine;
import com.soda.risk.engine.core.strategy.engine.ComputeEngine;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

import static org.mockito.Mockito.*;

class MessageConsumersTest {

    @Test
    void strategyConsumerComputesDisposesHitsAndAcknowledges() {
        ComputeEngine engine = mock(ComputeEngine.class);
        DisposerFlowService disposer = mock(DisposerFlowService.class);
        Acknowledgment ack = mock(Acknowledgment.class);
        StrategyHitResult hit = StrategyHitResult.builder().hit(true).traceId("trace-1").build();
        when(engine.execute("{\"value\":1}", "login", "kafka")).thenReturn(hit);
        StrategyMessageConsumer consumer = new StrategyMessageConsumer(engine, disposer);

        consumer.onStrategyMessage(new ConsumerRecord<>("strategy", 0, 1, "login", "{\"value\":1}"), ack);

        verify(disposer).execute(hit);
        verify(ack).acknowledge();
    }

    @Test
    void strategyConsumerAcknowledgesFailuresAndDisposerMessages() {
        ComputeEngine engine = mock(ComputeEngine.class);
        DisposerFlowService disposer = mock(DisposerFlowService.class);
        Acknowledgment ack = mock(Acknowledgment.class);
        when(engine.execute(anyString(), anyString(), anyString())).thenThrow(new IllegalStateException("bad"));
        StrategyMessageConsumer consumer = new StrategyMessageConsumer(engine, disposer);

        consumer.onStrategyMessage(new ConsumerRecord<>("strategy", 0, 1, "login", "bad"), ack);
        consumer.onDisposerMessage(new ConsumerRecord<>("disposer", 0, 2, "key", "{}"), ack);

        verify(ack, times(2)).acknowledge();
        verifyNoInteractions(disposer);
    }

    @Test
    void riskConsumerExecutesDecisionAndAcknowledges() {
        RiskDecisionEngine engine = mock(RiskDecisionEngine.class);
        Acknowledgment ack = mock(Acknowledgment.class);
        when(engine.execute("{}", "demo", "accountSecurity")).thenReturn(Response.success(
                RiskDecisionResult.builder().score(50).riskLevel("LOW").traceId("trace-2").build()));
        RiskDecisionMessageConsumer consumer = new RiskDecisionMessageConsumer(engine);

        consumer.onRiskDecisionMessage(new ConsumerRecord<>("risk", 0, 1, "demo", "{}"), ack);

        verify(engine).execute("{}", "demo", "accountSecurity");
        verify(ack).acknowledge();
    }
}

package com.soda.risk.engine.service.mq;

import com.soda.risk.engine.api.dto.Response;
import com.soda.risk.engine.api.dto.RiskDecisionResult;
import com.soda.risk.engine.core.riskdecision.engine.RiskDecisionEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 风险决策消息消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RiskDecisionMessageConsumer {

    private final RiskDecisionEngine riskDecisionEngine;

    @KafkaListener(
        topics = "${risk.engine.kafka.topic.risk_decision:risk_engine_risk_decision}",
        groupId = "${risk.engine.kafka.group.risk_decision:risk_decision_group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onRiskDecisionMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        long start = System.currentTimeMillis();
        try {
            String data = record.value();
            String openKey = record.key();

            log.info("Received risk decision message, topic={}, offset={}, key={}",
                    record.topic(), record.offset(), openKey);

            Response<RiskDecisionResult> result = riskDecisionEngine.execute(data, openKey, "accountSecurity");

            if (result.isSuccess() && result.getData() != null) {
                log.info("Risk decision result: score={}, level={}, traceId={}",
                        result.getData().getScore(),
                        result.getData().getRiskLevel(),
                        result.getData().getTraceId());
            }

            ack.acknowledge();
            log.debug("Risk decision message processed, cost={}ms", System.currentTimeMillis() - start);

        } catch (Exception e) {
            log.error("Process risk decision message failed, topic={}, offset={}",
                    record.topic(), record.offset(), e);
            throw new MessageProcessingException("Risk decision message processing failed", e);
        }
    }

    static class MessageProcessingException extends RuntimeException {
        MessageProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

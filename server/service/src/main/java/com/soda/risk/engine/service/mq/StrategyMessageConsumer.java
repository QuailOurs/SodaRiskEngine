package com.soda.risk.engine.service.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.api.dto.StrategyHitResult;
import com.soda.risk.engine.core.disposer.flow.DisposerFlowService;
import com.soda.risk.engine.core.strategy.engine.ComputeEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 策略引擎消息消费者
 * 消费Kafka消息，执行策略计算和处置
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StrategyMessageConsumer {

    private final ComputeEngine computeEngine;
    private final DisposerFlowService disposerFlowService;
    private final ObjectMapper objectMapper;

    /**
     * 消费策略引擎消息
     */
    @KafkaListener(
        topics = "${risk.engine.kafka.topic.strategy:risk_engine_strategy}",
        groupId = "${risk.engine.kafka.group.strategy:strategy_engine_group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onStrategyMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        long start = System.currentTimeMillis();
        try {
            String data = record.value();
            String sceneKey = record.key();
            
            log.info("Received strategy message, topic={}, partition={}, offset={}, key={}",
                    record.topic(), record.partition(), record.offset(), sceneKey);

            // 执行策略计算
            StrategyHitResult result = computeEngine.execute(data, sceneKey, "kafka");

            // 如果命中，执行处置
            if (result.isHit()) {
                result.setDisposerResponse(disposerFlowService.execute(result));
                log.warn("Strategy hit! sceneKey={}, traceId={}, cost={}ms",
                        sceneKey, result.getTraceId(), result.getCostMs());
            }

            ack.acknowledge();
            log.debug("Strategy message processed, cost={}ms", System.currentTimeMillis() - start);

        } catch (Exception e) {
            log.error("Process strategy message failed, topic={}, offset={}",
                    record.topic(), record.offset(), e);
            throw new MessageProcessingException("Strategy message processing failed", e);
        }
    }

    /**
     * 消费处置引擎消息
     */
    @KafkaListener(
        topics = "${risk.engine.kafka.topic.disposer:risk_engine_disposer}",
        groupId = "${risk.engine.kafka.group.disposer:disposer_engine_group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onDisposerMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        long start = System.currentTimeMillis();
        try {
            String data = record.value();
            log.info("Received disposer message, topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());

            StrategyHitResult hitResult = objectMapper.readValue(data, StrategyHitResult.class);
            disposerFlowService.execute(hitResult);

            ack.acknowledge();
            log.debug("Disposer message processed, cost={}ms", System.currentTimeMillis() - start);

        } catch (Exception e) {
            log.error("Process disposer message failed, topic={}, offset={}",
                    record.topic(), record.offset(), e);
            throw new MessageProcessingException("Disposer message processing failed", e);
        }
    }

    static class MessageProcessingException extends RuntimeException {
        MessageProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

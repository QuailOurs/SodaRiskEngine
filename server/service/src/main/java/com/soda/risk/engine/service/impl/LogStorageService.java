package com.soda.risk.engine.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志存储服务
 * 统一管理ES、HDFS、Kafka日志存储
 * Kafka和Redis在dev环境下可选
 */
@Slf4j
@Service
public class LogStorageService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;
    private final boolean kafkaAvailable;
    private final boolean redisAvailable;

    // 内存降级
    private final Map<String, String> memoryLogStore = new ConcurrentHashMap<>();

    private static final String LOG_TOPIC = "risk_engine_logs";
    private static final String LOG_REDIS_PREFIX = "soda:log:";

    public LogStorageService(ObjectMapper objectMapper,
                             @Autowired(required = false) KafkaTemplate<String, String> kafkaTemplate,
                             @Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
        this.kafkaAvailable = kafkaTemplate != null;
        this.redisAvailable = redisTemplate != null;
        if (!kafkaAvailable) {
            log.warn("Kafka not available, log storage will use in-memory fallback");
        }
        if (!redisAvailable) {
            log.warn("Redis not available, log query will use in-memory fallback");
        }
    }

    /**
     * 存储策略命中日志
     */
    public void storeStrategyHitLog(Map<String, Object> logData) {
        long start = System.currentTimeMillis();
        try {
            String traceId = (String) logData.getOrDefault("traceId", "");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            logData.put("timestamp", timestamp);
            logData.put("logType", "STRATEGY_HIT");

            String jsonLog = objectMapper.writeValueAsString(logData);

            if (kafkaAvailable) {
                kafkaTemplate.send(LOG_TOPIC, traceId, jsonLog);
            }
            if (redisAvailable) {
                redisTemplate.opsForValue().set(LOG_REDIS_PREFIX + traceId, jsonLog, 24 * 3600);
            } else {
                memoryLogStore.put(traceId, jsonLog);
            }

            MonitorFacade.insert("[log]StoreStrategyHit", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Store strategy hit log failed", e);
        }
    }

    /**
     * 存储处置日志
     */
    public void storeDisposerLog(Map<String, Object> logData) {
        long start = System.currentTimeMillis();
        try {
            String traceId = (String) logData.getOrDefault("traceId", "");
            logData.put("logType", "DISPOSER");
            logData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            String jsonLog = objectMapper.writeValueAsString(logData);
            if (kafkaAvailable) {
                kafkaTemplate.send(LOG_TOPIC, traceId, jsonLog);
            }

            MonitorFacade.insert("[log]StoreDisposer", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Store disposer log failed", e);
        }
    }

    /**
     * 存储风险决策日志
     */
    public void storeRiskDecisionLog(Map<String, Object> logData) {
        long start = System.currentTimeMillis();
        try {
            String traceId = (String) logData.getOrDefault("traceId", "");
            logData.put("logType", "RISK_DECISION");
            logData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            String jsonLog = objectMapper.writeValueAsString(logData);
            if (kafkaAvailable) {
                kafkaTemplate.send(LOG_TOPIC, traceId, jsonLog);
            }

            MonitorFacade.insert("[log]StoreRiskDecision", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Store risk decision log failed", e);
        }
    }

    /**
     * 根据TraceId查询日志
     */
    public String queryByTraceId(String traceId) {
        try {
            if (redisAvailable) {
                return redisTemplate.opsForValue().get(LOG_REDIS_PREFIX + traceId);
            }
            return memoryLogStore.get(traceId);
        } catch (Exception e) {
            log.error("Query log by traceId failed", e);
            return null;
        }
    }
}

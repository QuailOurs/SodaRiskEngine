package com.soda.risk.engine.core.strategy.feature;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** 为特征作业提供有界线程池，避免外部特征服务阻塞决策线程或无限堆积任务。 */
@Configuration
public class FeatureExecutionConfiguration {

    @Bean(name = "featureQueryExecutor", destroyMethod = "shutdown")
    public ExecutorService featureQueryExecutor(
            @Value("${soda.engine.feature-workers:8}") int workers,
            @Value("${soda.engine.feature-queue-capacity:256}") int queueCapacity) {
        int poolSize = Math.max(1, workers);
        int capacity = Math.max(poolSize, queueCapacity);
        AtomicInteger sequence = new AtomicInteger();
        ThreadFactory threadFactory = task -> {
            Thread thread = new Thread(task, "soda-feature-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
        return new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(capacity), threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
    }
}

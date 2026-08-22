package com.soda.risk.engine.common.monitor;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 性能监控切面
 */
@Slf4j
@Aspect
@Component
public class PerformanceMonitorAspect {

    @Around("@annotation(performanceMonitor)")
    public Object around(ProceedingJoinPoint joinPoint, PerformanceMonitor performanceMonitor) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long cost = System.currentTimeMillis() - start;
            MonitorFacade.insert(performanceMonitor.key(), cost);
            if (cost > 1000) {
                log.warn("Slow method: {}, cost: {}ms", performanceMonitor.key(), cost);
            }
        }
    }
}

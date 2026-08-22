package com.soda.risk.engine.common.monitor;

import java.lang.annotation.*;

/**
 * 性能监控注解。
 * 配合AOP自动记录方法耗时
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PerformanceMonitor {
    /** 监控Key */
    String key();
}

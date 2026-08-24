package com.soda.risk.engine.config.sync;

/** 单个配置域的同步结果。 */
public record ConfigSyncStepResult(String domain, boolean success, long costMs, String error) {
}

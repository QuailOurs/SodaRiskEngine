package com.soda.risk.engine.config.sync;

import java.util.List;

/** 一轮配置同步的结构化报告。 */
public record ConfigSyncReport(boolean success, boolean skipped, long costMs,
                               List<ConfigSyncStepResult> steps) {

    public ConfigSyncReport {
        steps = List.copyOf(steps);
    }

    public static ConfigSyncReport busy() {
        return new ConfigSyncReport(false, true, 0L, List.of());
    }
}

package com.soda.risk.engine.config.sync;

import java.util.Objects;

/** 一个可独立同步、可排序的配置域。新增配置类型时只需注册新的贡献者。 */
public interface ConfigSyncContributor {

    String domain();

    int order();

    void sync();

    static ConfigSyncContributor of(String domain, int order, Runnable action) {
        Objects.requireNonNull(domain, "domain");
        Objects.requireNonNull(action, "action");
        return new ConfigSyncContributor() {
            @Override public String domain() { return domain; }
            @Override public int order() { return order; }
            @Override public void sync() { action.run(); }
        };
    }
}

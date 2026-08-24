package com.soda.risk.engine.config.controller;

import com.soda.risk.engine.api.dto.Response;
import com.soda.risk.engine.config.sync.ConfigSyncCoordinator;
import com.soda.risk.engine.config.sync.ConfigSyncReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 配置同步Controller - 统一管理所有配置的Redis同步
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/config/sync")
@RequiredArgsConstructor
public class ConfigSyncController {

    private final ConfigSyncCoordinator syncCoordinator;

    /**
     * 同步所有配置到Redis
     */
    @PostMapping("/all")
    public Response<ConfigSyncReport> syncAll() {
        return respond(syncCoordinator.syncAll());
    }

    /**
     * 同步策略配置
     */
    @PostMapping("/strategy")
    public Response<ConfigSyncReport> syncStrategy() {
        return respond(syncCoordinator.syncDomains(java.util.List.of("strategy")));
    }

    /**
     * 同步规则配置
     */
    @PostMapping("/rule")
    public Response<ConfigSyncReport> syncRule() {
        return respond(syncCoordinator.syncDomains(java.util.List.of("rule")));
    }

    /**
     * 同步场景配置
     */
    @PostMapping("/scene")
    public Response<ConfigSyncReport> syncScene() {
        return respond(syncCoordinator.syncDomains(java.util.List.of("scene")));
    }

    /**
     * 同步特征配置
     */
    @PostMapping("/feature")
    public Response<ConfigSyncReport> syncFeature() {
        return respond(syncCoordinator.syncDomains(java.util.List.of("feature")));
    }

    /**
     * 同步处置配置
     */
    @PostMapping("/disposer")
    public Response<ConfigSyncReport> syncDisposer() {
        return respond(syncCoordinator.syncDomains(java.util.List.of("disposer")));
    }

    /**
     * 同步风险决策配置
     */
    @PostMapping("/risk")
    public Response<ConfigSyncReport> syncRisk() {
        return respond(syncCoordinator.syncDomains(java.util.List.of("risk", "black-white")));
    }

    private Response<ConfigSyncReport> respond(ConfigSyncReport report) {
        log.info("Config sync completed: success={}, skipped={}, costMs={}, steps={}",
                report.success(), report.skipped(), report.costMs(), report.steps().size());
        return Response.success(report);
    }
}

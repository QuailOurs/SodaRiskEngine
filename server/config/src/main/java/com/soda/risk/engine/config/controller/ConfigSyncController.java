package com.soda.risk.engine.config.controller;

import com.soda.risk.engine.api.dto.Response;
import com.soda.risk.engine.config.disposer.DisposerConfigService;
import com.soda.risk.engine.config.feature.BaseInfoFeatureService;
import com.soda.risk.engine.config.riskdecision.BlackWhiteListService;
import com.soda.risk.engine.config.riskdecision.RiskConfigService;
import com.soda.risk.engine.config.rule.RuleService;
import com.soda.risk.engine.config.scene.SceneService;
import com.soda.risk.engine.config.strategy.StrategyService;
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

    private final StrategyService strategyService;
    private final RuleService ruleService;
    private final SceneService sceneService;
    private final BaseInfoFeatureService baseInfoFeatureService;
    private final DisposerConfigService disposerConfigService;
    private final RiskConfigService riskConfigService;
    private final BlackWhiteListService blackWhiteListService;

    /**
     * 同步所有配置到Redis
     */
    @PostMapping("/all")
    public Response<String> syncAll() {
        long start = System.currentTimeMillis();
        try {
            strategyService.syncToRedis();
            ruleService.syncToRedis();
            sceneService.syncToRedis();
            baseInfoFeatureService.syncToRedis();
            disposerConfigService.syncToRedis();
            riskConfigService.syncToRedis();
            blackWhiteListService.syncToRedis();

            long cost = System.currentTimeMillis() - start;
            String msg = "All configs synced to Redis, cost: " + cost + "ms";
            log.info(msg);
            return Response.success(msg);

        } catch (Exception e) {
            log.error("Sync all configs failed", e);
            return Response.fail(-1, "Sync failed: " + e.getMessage());
        }
    }

    /**
     * 同步策略配置
     */
    @PostMapping("/strategy")
    public Response<String> syncStrategy() {
        strategyService.syncToRedis();
        return Response.success("Strategy config synced");
    }

    /**
     * 同步规则配置
     */
    @PostMapping("/rule")
    public Response<String> syncRule() {
        ruleService.syncToRedis();
        return Response.success("Rule config synced");
    }

    /**
     * 同步场景配置
     */
    @PostMapping("/scene")
    public Response<String> syncScene() {
        sceneService.syncToRedis();
        return Response.success("Scene config synced");
    }

    /**
     * 同步特征配置
     */
    @PostMapping("/feature")
    public Response<String> syncFeature() {
        baseInfoFeatureService.syncToRedis();
        return Response.success("Feature config synced");
    }

    /**
     * 同步处置配置
     */
    @PostMapping("/disposer")
    public Response<String> syncDisposer() {
        disposerConfigService.syncToRedis();
        return Response.success("Disposer config synced");
    }

    /**
     * 同步风险决策配置
     */
    @PostMapping("/risk")
    public Response<String> syncRisk() {
        riskConfigService.syncToRedis();
        blackWhiteListService.syncToRedis();
        return Response.success("Risk config synced");
    }
}

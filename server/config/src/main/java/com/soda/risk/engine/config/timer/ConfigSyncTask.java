package com.soda.risk.engine.config.timer;

import com.soda.risk.engine.config.disposer.DisposerConfigService;
import com.soda.risk.engine.config.feature.BaseInfoFeatureService;
import com.soda.risk.engine.config.riskdecision.BlackWhiteListService;
import com.soda.risk.engine.config.riskdecision.RiskConfigService;
import com.soda.risk.engine.config.rule.RuleService;
import com.soda.risk.engine.config.scene.SceneService;
import com.soda.risk.engine.config.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 配置同步定时任务
 * 统一将数据库配置同步到Redis
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigSyncTask {

    private final StrategyService strategyService;
    private final RuleService ruleService;
    private final SceneService sceneService;
    private final BaseInfoFeatureService baseInfoFeatureService;
    private final DisposerConfigService disposerConfigService;
    private final RiskConfigService riskConfigService;
    private final BlackWhiteListService blackWhiteListService;

    /**
     * 每5分钟同步所有配置
     */
    @Scheduled(fixedRate = 300000)
    public void syncAllConfigs() {
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
            log.info("All configs synced to Redis, cost: {}ms", cost);

        } catch (Exception e) {
            log.error("Sync all configs failed", e);
        }
    }

    /**
     * 每小时同步黑白名单
     */
    @Scheduled(fixedRate = 3600000)
    public void syncBlackWhiteList() {
        try {
            blackWhiteListService.syncToRedis();
            log.debug("BlackWhiteList synced to Redis");
        } catch (Exception e) {
            log.error("Sync BlackWhiteList failed", e);
        }
    }
}

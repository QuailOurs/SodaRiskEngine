package com.soda.risk.engine.config.sync;

import com.soda.risk.engine.config.disposer.DisposerConfigService;
import com.soda.risk.engine.config.feature.BaseInfoFeatureService;
import com.soda.risk.engine.config.riskdecision.BlackWhiteListService;
import com.soda.risk.engine.config.riskdecision.RiskConfigService;
import com.soda.risk.engine.config.rule.RuleService;
import com.soda.risk.engine.config.scene.SceneService;
import com.soda.risk.engine.config.strategy.StrategyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 默认配置域及其依赖顺序。规则先于策略发布，保证兼容缓存中的引用先就绪。 */
@Configuration(proxyBeanMethods = false)
public class ConfigSyncContributorConfiguration {

    @Bean
    ConfigSyncContributor sceneSyncContributor(SceneService service) {
        return ConfigSyncContributor.of("scene", 10, service::syncToRedis);
    }

    @Bean
    ConfigSyncContributor featureSyncContributor(BaseInfoFeatureService service) {
        return ConfigSyncContributor.of("feature", 20, service::syncToRedis);
    }

    @Bean
    ConfigSyncContributor ruleSyncContributor(RuleService service) {
        return ConfigSyncContributor.of("rule", 30, service::syncToRedis);
    }

    @Bean
    ConfigSyncContributor strategySyncContributor(StrategyService service) {
        return ConfigSyncContributor.of("strategy", 40, service::syncToRedis);
    }

    @Bean
    ConfigSyncContributor disposerSyncContributor(DisposerConfigService service) {
        return ConfigSyncContributor.of("disposer", 50, service::syncToRedis);
    }

    @Bean
    ConfigSyncContributor riskSyncContributor(RiskConfigService service) {
        return ConfigSyncContributor.of("risk", 60, service::syncToRedis);
    }

    @Bean
    ConfigSyncContributor blackWhiteSyncContributor(BlackWhiteListService service) {
        return ConfigSyncContributor.of("black-white", 70, service::syncToRedis);
    }
}

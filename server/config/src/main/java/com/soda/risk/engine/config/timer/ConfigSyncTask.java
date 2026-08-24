package com.soda.risk.engine.config.timer;

import com.soda.risk.engine.config.sync.ConfigSyncCoordinator;
import com.soda.risk.engine.config.sync.ConfigSyncReport;
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

    private final ConfigSyncCoordinator syncCoordinator;

    /**
     * 每5分钟同步所有配置
     */
    @Scheduled(fixedRate = 300000)
    public void syncAllConfigs() {
        ConfigSyncReport report = syncCoordinator.syncAll();
        log.info("Scheduled config sync completed: success={}, skipped={}, costMs={}",
                report.success(), report.skipped(), report.costMs());
    }

    /**
     * 每小时同步黑白名单
     */
    @Scheduled(fixedRate = 3600000)
    public void syncBlackWhiteList() {
        ConfigSyncReport report = syncCoordinator.syncDomains(java.util.List.of("black-white"));
        log.debug("Scheduled black-white sync completed: success={}, skipped={}, costMs={}",
                report.success(), report.skipped(), report.costMs());
    }
}

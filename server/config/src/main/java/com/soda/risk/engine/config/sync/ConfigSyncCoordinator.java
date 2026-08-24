package com.soda.risk.engine.config.sync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 配置同步总编排器：统一依赖顺序、防止定时与手工同步重叠，并隔离单个配置域失败。
 */
@Slf4j
@Service
public class ConfigSyncCoordinator {

    private final List<ConfigSyncContributor> contributors;
    private final ReentrantLock syncLock = new ReentrantLock();

    public ConfigSyncCoordinator(List<ConfigSyncContributor> contributors) {
        this.contributors = contributors.stream()
                .sorted(Comparator.comparingInt(ConfigSyncContributor::order)
                        .thenComparing(ConfigSyncContributor::domain))
                .toList();
    }

    public ConfigSyncReport syncAll() {
        return syncDomains(contributors.stream().map(ConfigSyncContributor::domain).toList());
    }

    public ConfigSyncReport syncDomains(Collection<String> domains) {
        if (!syncLock.tryLock()) {
            log.info("Config sync skipped because another sync is running");
            return ConfigSyncReport.busy();
        }
        long startedAt = System.currentTimeMillis();
        List<ConfigSyncStepResult> results = new ArrayList<>();
        try {
            Set<String> selected = new LinkedHashSet<>(domains);
            for (ConfigSyncContributor contributor : contributors) {
                if (!selected.contains(contributor.domain())) continue;
                long stepStartedAt = System.currentTimeMillis();
                try {
                    contributor.sync();
                    results.add(new ConfigSyncStepResult(contributor.domain(), true,
                            System.currentTimeMillis() - stepStartedAt, null));
                } catch (Exception e) {
                    results.add(new ConfigSyncStepResult(contributor.domain(), false,
                            System.currentTimeMillis() - stepStartedAt, safeMessage(e)));
                    log.error("Config sync failed, domain={}", contributor.domain(), e);
                }
            }
            Set<String> known = contributors.stream().map(ConfigSyncContributor::domain)
                    .collect(java.util.stream.Collectors.toSet());
            selected.stream().filter(domain -> !known.contains(domain)).forEach(domain ->
                    results.add(new ConfigSyncStepResult(domain, false, 0L, "unknown config domain")));
            boolean success = results.stream().allMatch(ConfigSyncStepResult::success);
            return new ConfigSyncReport(success, false,
                    System.currentTimeMillis() - startedAt, results);
        } finally {
            syncLock.unlock();
        }
    }

    private String safeMessage(Exception error) {
        String message = error.getMessage();
        return message == null || message.isBlank() ? error.getClass().getSimpleName() : message;
    }
}

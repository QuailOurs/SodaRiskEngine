package com.soda.risk.engine.config.sync;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigSyncCoordinatorTest {

    @Test
    void executesContributorsInDependencyOrder() {
        List<String> calls = new ArrayList<>();
        ConfigSyncCoordinator coordinator = new ConfigSyncCoordinator(List.of(
                ConfigSyncContributor.of("strategy", 40, () -> calls.add("strategy")),
                ConfigSyncContributor.of("scene", 10, () -> calls.add("scene")),
                ConfigSyncContributor.of("rule", 30, () -> calls.add("rule"))));

        ConfigSyncReport report = coordinator.syncAll();

        assertThat(calls).containsExactly("scene", "rule", "strategy");
        assertThat(report.success()).isTrue();
        assertThat(report.steps()).extracting(ConfigSyncStepResult::domain)
                .containsExactly("scene", "rule", "strategy");
    }

    @Test
    void continuesAfterOneDomainFailsAndReturnsStructuredFailure() {
        List<String> calls = new ArrayList<>();
        ConfigSyncCoordinator coordinator = new ConfigSyncCoordinator(List.of(
                ConfigSyncContributor.of("scene", 10, () -> { throw new IllegalStateException("db down"); }),
                ConfigSyncContributor.of("rule", 20, () -> calls.add("rule"))));

        ConfigSyncReport report = coordinator.syncAll();

        assertThat(calls).containsExactly("rule");
        assertThat(report.success()).isFalse();
        assertThat(report.steps()).hasSize(2);
        assertThat(report.steps().get(0).success()).isFalse();
        assertThat(report.steps().get(0).error()).isEqualTo("db down");
        assertThat(report.steps().get(1).success()).isTrue();
    }

    @Test
    void selectsRequestedDomainsAndReportsUnknownOnes() {
        List<String> calls = new ArrayList<>();
        ConfigSyncCoordinator coordinator = new ConfigSyncCoordinator(List.of(
                ConfigSyncContributor.of("scene", 10, () -> calls.add("scene")),
                ConfigSyncContributor.of("rule", 20, () -> calls.add("rule"))));

        ConfigSyncReport report = coordinator.syncDomains(List.of("rule", "missing"));

        assertThat(calls).containsExactly("rule");
        assertThat(report.success()).isFalse();
        assertThat(report.steps()).extracting(ConfigSyncStepResult::domain)
                .containsExactly("rule", "missing");
    }

    @Test
    void skipsOverlappingSyncInsteadOfRunningContributorsTwice() throws Exception {
        CountDownLatch firstEntered = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        ConfigSyncCoordinator coordinator = new ConfigSyncCoordinator(List.of(
                ConfigSyncContributor.of("scene", 10, () -> {
                    firstEntered.countDown();
                    try {
                        if (!releaseFirst.await(2, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("test release timed out");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("interrupted", e);
                    }
                })));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<ConfigSyncReport> running = executor.submit(coordinator::syncAll);
            assertThat(firstEntered.await(1, TimeUnit.SECONDS)).isTrue();

            ConfigSyncReport overlapping = coordinator.syncAll();

            assertThat(overlapping.skipped()).isTrue();
            assertThat(overlapping.success()).isFalse();
            assertThat(overlapping.steps()).isEmpty();
            releaseFirst.countDown();
            assertThat(running.get(1, TimeUnit.SECONDS).success()).isTrue();
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
        }
    }
}

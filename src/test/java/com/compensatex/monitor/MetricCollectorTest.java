package com.compensatex.monitor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MetricCollectorTest {

    @Test
    void accumulatesMetricSnapshot() {
        MetricCollector collector = new MetricCollector();

        collector.recordSuccess();
        collector.recordSuccess();
        collector.recordFailure();
        collector.recordRetry();
        collector.recordRetry();
        collector.recordRetry();

        assertThat(collector.snapshot()).isEqualTo("success=2,failure=1,retry=3");
    }
}

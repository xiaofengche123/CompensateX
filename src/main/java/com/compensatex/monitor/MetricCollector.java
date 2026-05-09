package com.compensatex.monitor;

import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 补偿指标采集器，用于记录成功、失败和重试次数。
 */
public class MetricCollector {

    private static final Logger log = LoggerFactory.getLogger(MetricCollector.class);

    private final AtomicLong successCount = new AtomicLong();
    private final AtomicLong failureCount = new AtomicLong();
    private final AtomicLong retryCount = new AtomicLong();

    /**
     * 记录成功事件。
     */
    public void recordSuccess() {
        long value = successCount.incrementAndGet();
        log.info("Compensate metric success={}", value);
    }

    /**
     * 记录失败事件。
     */
    public void recordFailure() {
        long value = failureCount.incrementAndGet();
        log.warn("Compensate metric failure={}", value);
    }

    /**
     * 记录重试事件。
     */
    public void recordRetry() {
        long value = retryCount.incrementAndGet();
        log.info("Compensate metric retry={}", value);
    }

    /**
     * 导出指标快照。
     *
     * @return 指标字符串
     */
    public String snapshot() {
        return "success=" + successCount.get() + ",failure=" + failureCount.get() + ",retry=" + retryCount.get();
    }
}

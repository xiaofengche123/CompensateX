package com.compensatex.executor;

import com.compensatex.core.CompensateContext;
import com.compensatex.monitor.MetricCollector;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 异步重试执行器。
 * <p>
 * 线程池参数固定为：核心线程 2、最大线程 5、队列容量 100。
 */
public class AsyncRetryExecutor {

    private static final Logger log = LoggerFactory.getLogger(AsyncRetryExecutor.class);
    private static final Path FAILED_LOG_PATH = Paths.get("failed_compensations.log");

    private final MetricCollector metricCollector;
    private final ThreadPoolExecutor retryPool;
    private final ScheduledExecutorService scheduler;

    /**
     * 创建异步重试执行器。
     *
     * @param metricCollector 指标收集器
     */
    public AsyncRetryExecutor(MetricCollector metricCollector) {
        this.metricCollector = metricCollector;
        this.retryPool = new ThreadPoolExecutor(
                2,
                5,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadPoolExecutor.AbortPolicy()
        );
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * 提交补偿重试任务。
     *
     * @param context      补偿上下文
     * @param task         补偿逻辑
     * @param retryTimes   重试次数
     * @param retryDelayMs 重试间隔毫秒
     */
    public void submitRetry(CompensateContext context,
                            Callable<Object> task,
                            int retryTimes,
                            long retryDelayMs) {
        scheduler.schedule(() -> retry(context, task, retryTimes, retryDelayMs, 1), retryDelayMs, TimeUnit.MILLISECONDS);
    }

    private void retry(CompensateContext context,
                       Callable<Object> task,
                       int retryTimes,
                       long retryDelayMs,
                       int current) {
        retryPool.submit(() -> {
            try {
                metricCollector.recordRetry();
                task.call();
                metricCollector.recordSuccess();
                log.info("Compensate retry success. requestId={}, attempt={}", context.getRequestId(), current);
            } catch (Exception ex) {
                log.error("Compensate retry failed. requestId={}, attempt={}", context.getRequestId(), current, ex);
                if (current < retryTimes) {
                    scheduler.schedule(
                            () -> retry(context, task, retryTimes, retryDelayMs, current + 1),
                            retryDelayMs,
                            TimeUnit.MILLISECONDS
                    );
                } else {
                    metricCollector.recordFailure();
                    appendFailedLog(context, ex);
                }
            }
        });
    }

    private void appendFailedLog(CompensateContext context, Exception ex) {
        String line = String.format(
                "[%s] requestId=%s, method=%s, idempotent=%s, reason=%s%n",
                LocalDateTime.now(),
                context.getRequestId(),
                context.getMethod().getName(),
                context.getIdempotentValue(),
                ex.getMessage()
        );
        try {
            Files.write(
                    FAILED_LOG_PATH,
                    line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ioException) {
            log.error("Write failed_compensations.log error", ioException);
        }
    }
}

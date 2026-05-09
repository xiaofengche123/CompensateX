package com.compensatex.autoconfig;

import com.compensatex.aop.CompensableInterceptor;
import com.compensatex.core.CompensateManager;
import com.compensatex.dubbo.DubboGenericInvoker;
import com.compensatex.executor.AsyncRetryExecutor;
import com.compensatex.idempotent.IdempotentChecker;
import com.compensatex.monitor.MetricCollector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * CompensateX 自动配置，注册核心组件 Bean。
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class CompensateXAutoConfiguration {

    /**
     * 指标采集器 Bean。
     *
     * @return 指标采集器
     */
    @Bean
    @ConditionalOnMissingBean
    public MetricCollector metricCollector() {
        return new MetricCollector();
    }

    /**
     * 幂等检查器 Bean。
     *
     * @return 幂等检查器
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentChecker idempotentChecker(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        return new IdempotentChecker(redisTemplateProvider.getIfAvailable());
    }

    /**
     * Dubbo 泛化调用器 Bean。
     *
     * @return Dubbo 调用器
     */
    @Bean
    @ConditionalOnMissingBean
    public DubboGenericInvoker dubboGenericInvoker() {
        return new DubboGenericInvoker();
    }

    /**
     * 异步重试执行器 Bean。
     *
     * @param metricCollector 指标采集器
     * @return 异步重试执行器
     */
    @Bean
    @ConditionalOnMissingBean
    public AsyncRetryExecutor asyncRetryExecutor(MetricCollector metricCollector) {
        return new AsyncRetryExecutor(metricCollector);
    }

    /**
     * 补偿管理器 Bean。
     *
     * @param asyncRetryExecutor 异步重试执行器
     * @param idempotentChecker 幂等检查器
     * @param metricCollector 指标采集器
     * @param dubboGenericInvoker Dubbo 调用器
     * @return 补偿管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public CompensateManager compensateManager(AsyncRetryExecutor asyncRetryExecutor,
                                               IdempotentChecker idempotentChecker,
                                               MetricCollector metricCollector,
                                               DubboGenericInvoker dubboGenericInvoker) {
        return new CompensateManager(asyncRetryExecutor, idempotentChecker, metricCollector, dubboGenericInvoker);
    }

    /**
     * AOP 拦截器 Bean。
     *
     * @param compensateManager 补偿管理器
     * @return 拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public CompensableInterceptor compensableInterceptor(CompensateManager compensateManager) {
        return new CompensableInterceptor(compensateManager);
    }
}

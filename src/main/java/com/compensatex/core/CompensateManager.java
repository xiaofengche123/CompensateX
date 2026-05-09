package com.compensatex.core;

import com.compensatex.annotation.Compensable;
import com.compensatex.dubbo.DubboGenericInvoker;
import com.compensatex.executor.AsyncRetryExecutor;
import com.compensatex.idempotent.IdempotentChecker;
import com.compensatex.monitor.MetricCollector;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

/**
 * 补偿流程核心管理器，负责执行、重试、幂等判断、回退路由与失败落盘。
 */
public class CompensateManager {

    private static final Logger log = LoggerFactory.getLogger(CompensateManager.class);

    private final AsyncRetryExecutor asyncRetryExecutor;
    private final IdempotentChecker idempotentChecker;
    private final MetricCollector metricCollector;
    private final DubboGenericInvoker dubboGenericInvoker;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * 创建补偿管理器。
     *
     * @param asyncRetryExecutor 异步重试执行器
     * @param idempotentChecker  幂等检查器
     * @param metricCollector    指标收集器
     * @param dubboGenericInvoker Dubbo 泛化调用器
     */
    public CompensateManager(AsyncRetryExecutor asyncRetryExecutor,
                             IdempotentChecker idempotentChecker,
                             MetricCollector metricCollector,
                             DubboGenericInvoker dubboGenericInvoker) {
        this.asyncRetryExecutor = asyncRetryExecutor;
        this.idempotentChecker = idempotentChecker;
        this.metricCollector = metricCollector;
        this.dubboGenericInvoker = dubboGenericInvoker;
    }

    /**
     * 执行被拦截的方法，失败后触发补偿。
     *
     * @param joinPoint   切点
     * @param compensable 注解
     * @return 原始执行结果或补偿执行结果
     * @throws Throwable 调用异常
     */
    public Object execute(ProceedingJoinPoint joinPoint, Compensable compensable) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String idempotentValue = resolveIdempotentValue(compensable.idempotentKey(), joinPoint.getArgs());
        CompensateContext context = new CompensateContext(
                joinPoint.getTarget(),
                method,
                joinPoint.getArgs(),
                compensable,
                idempotentValue
        );

        if (StringUtils.hasText(idempotentValue) && !idempotentChecker.checkAndMark(idempotentValue)) {
            log.warn("Skip duplicated compensate request. idempotentKey={}", idempotentValue);
            return null;
        }

        try {
            Object result = joinPoint.proceed();
            metricCollector.recordSuccess();
            return result;
        } catch (Throwable ex) {
            log.error("Original invocation failed, start compensate. requestId={}", context.getRequestId(), ex);
            Callable<Object> task = () -> executeFallback(context);
            try {
                Object fallbackResult = task.call();
                metricCollector.recordSuccess();
                return fallbackResult;
            } catch (Exception firstFail) {
                metricCollector.recordFailure();
                asyncRetryExecutor.submitRetry(
                        context,
                        task,
                        compensable.retryTimes(),
                        compensable.retryDelay()
                );
                throw ex;
            }
        }
    }

    private Object executeFallback(CompensateContext context) throws Exception {
        Compensable compensable = context.getCompensable();
        if (StringUtils.hasText(compensable.fallbackMethod())) {
            return invokeLocalFallback(context, compensable.fallbackMethod());
        }
        if (StringUtils.hasText(compensable.remoteFallback())) {
            return dubboGenericInvoker.invoke(compensable.remoteFallback(),
                    context.getMethod().getName(),
                    context.getMethod().getParameterTypes(),
                    context.getArgs());
        }
        throw new IllegalStateException("No fallback configured for method: " + context.getMethod().getName());
    }

    private Object invokeLocalFallback(CompensateContext context, String fallbackMethod) throws Exception {
        Method method = context.getTarget().getClass()
                .getMethod(fallbackMethod, context.getMethod().getParameterTypes());
        return method.invoke(context.getTarget(), context.getArgs());
    }

    private String resolveIdempotentValue(String idempotentKey, Object[] args) {
        if (!StringUtils.hasText(idempotentKey)) {
            return "";
        }
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < args.length; i++) {
            context.setVariable("p" + i, args[i]);
        }
        Object value = expressionParser.parseExpression(idempotentKey).getValue(context);
        return value == null ? "" : String.valueOf(value);
    }
}

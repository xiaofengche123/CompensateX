package com.compensatex.core;

import com.compensatex.annotation.Compensable;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * 补偿执行上下文，封装一次补偿任务的核心信息。
 */
public class CompensateContext {

    private final String requestId;
    private final Object target;
    private final Method method;
    private final Object[] args;
    private final Compensable compensable;
    private final String idempotentValue;
    private final LocalDateTime createTime;

    /**
     * 创建补偿上下文。
     *
     * @param target          目标对象
     * @param method          目标方法
     * @param args            方法参数
     * @param compensable     注解配置
     * @param idempotentValue 解析后的幂等值
     */
    public CompensateContext(Object target,
                             Method method,
                             Object[] args,
                             Compensable compensable,
                             String idempotentValue) {
        this.requestId = UUID.randomUUID().toString();
        this.target = target;
        this.method = method;
        this.args = args == null ? new Object[0] : Arrays.copyOf(args, args.length);
        this.compensable = compensable;
        this.idempotentValue = idempotentValue;
        this.createTime = LocalDateTime.now();
    }

    public String getRequestId() {
        return requestId;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }

    public Compensable getCompensable() {
        return compensable;
    }

    public String getIdempotentValue() {
        return idempotentValue;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }
}

package com.compensatex.aop;

import com.compensatex.annotation.Compensable;
import com.compensatex.core.CompensateManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

/**
 * 拦截 {@link Compensable} 注解的方法并交由补偿管理器统一处理。
 */
@Aspect
@Order(0)
public class CompensableInterceptor {

    private final CompensateManager compensateManager;

    /**
     * 创建拦截器。
     *
     * @param compensateManager 补偿管理器
     */
    public CompensableInterceptor(CompensateManager compensateManager) {
        this.compensateManager = compensateManager;
    }

    /**
     * 环绕通知：执行目标方法，失败时触发补偿逻辑。
     *
     * @param joinPoint   切点信息
     * @param compensable 注解配置
     * @return 目标方法返回值或补偿结果
     * @throws Throwable 执行过程异常
     */
    @Around("@annotation(compensable)")
    public Object around(ProceedingJoinPoint joinPoint, Compensable compensable) throws Throwable {
        return compensateManager.execute(joinPoint, compensable);
    }
}

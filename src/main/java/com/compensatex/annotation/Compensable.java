package com.compensatex.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明一个方法在失败时可触发补偿流程。
 * <p>
 * 当被该注解标记的方法抛出异常时，框架会按照配置进行本地/远程回退以及异步重试。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Compensable {

    /**
     * 最大重试次数（不包含首次执行）。
     *
     * @return 重试次数
     */
    int retryTimes() default 3;

    /**
     * 每次重试的延迟时间，单位毫秒。
     *
     * @return 重试延迟毫秒数
     */
    long retryDelay() default 1000L;

    /**
     * 本地回退方法名，默认空表示不启用本地回退。
     * 回退方法建议与原方法参数一致。
     *
     * @return 回退方法名
     */
    String fallbackMethod() default "";

    /**
     * 远程回退服务接口全限定名，默认空表示不启用远程回退。
     *
     * @return 远程服务接口名
     */
    String remoteFallback() default "";

    /**
     * 幂等键 SpEL 表达式，例如：#p0.userId。
     *
     * @return 幂等键表达式
     */
    String idempotentKey() default "";
}

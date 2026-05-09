# CompensateX

CompensateX 是一个通用 RPC 补偿组件示例工程，基于 **Spring Boot 2.7.18** + **Dubbo 泛化调用**，提供以下能力：

- `@Compensable` 注解驱动补偿流程
- 异步重试（线程池：核心 2、最大 5、队列 100）
- 幂等控制（Redis 分布式幂等，支持 SpEL，如 `#p0.userId`）
- 监控指标采集（成功/失败/重试）
- 最终失败写入 `failed_compensations.log`

## 项目结构

- `com.compensatex.annotation`：补偿注解定义
- `com.compensatex.aop`：AOP 拦截器
- `com.compensatex.core`：补偿管理与上下文
- `com.compensatex.executor`：异步重试执行器
- `com.compensatex.idempotent`：幂等检查器
- `com.compensatex.monitor`：指标采集器
- `com.compensatex.dubbo`：Dubbo 泛化调用器
- `com.compensatex.demo`：演示服务与启动类
- `com.compensatex.autoconfig`：Starter 自动配置

## 快速启动

```bash
./mvnw clean package
./mvnw spring-boot:run -Dspring-boot.run.main-class=com.compensatex.demo.DemoClient
```

或者直接运行：

```bash
./mvnw exec:java -Dexec.mainClass=ManualTest
```

## 使用示例

```java
@Compensable(
    retryTimes = 3,
    retryDelay = 1500L,
    fallbackMethod = "localFallback",
    remoteFallback = "com.foo.RemoteService",
    idempotentKey = "#p0.userId"
)
public String createOrder(Request req) {
    // 业务逻辑
}
```

## 行为说明

1. 业务方法执行成功：记录成功指标。
2. 业务方法执行失败：立即尝试本地/远程 fallback。
3. fallback 失败：进入异步重试，按照 `retryTimes + retryDelay` 执行。
4. 所有重试失败：写入 `failed_compensations.log`。

## 注意事项

- 默认启用 Redis 幂等；当 Redis 不可用时会自动降级到内存模式。
- Dubbo 泛化调用依赖运行时 Dubbo 配置（注册中心、协议等）。
- `application.properties` 已提供最简示例配置。

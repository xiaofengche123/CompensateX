package com.compensatex.demo;

import com.compensatex.annotation.Compensable;
import org.springframework.stereotype.Service;

/**
 * 演示业务服务，模拟失败触发补偿。
 */
@Service
public class DemoService {

    /**
     * 执行业务操作，固定抛异常以触发补偿。
     *
     * @param request 请求参数
     * @return 业务结果
     */
    @Compensable(
            retryTimes = 3,
            retryDelay = 1500L,
            fallbackMethod = "localFallback",
            remoteFallback = "",
            idempotentKey = "#p0.userId"
    )
    public String createOrder(DemoRequest request) {
        throw new RuntimeException("Simulated business exception for compensate test");
    }

    /**
     * 本地补偿方法，参数与原方法一致。
     *
     * @param request 请求参数
     * @return 补偿结果
     */
    public String localFallback(DemoRequest request) {
        return "LOCAL_FALLBACK_OK:" + request.getUserId();
    }

    /**
     * 演示请求对象。
     */
    public static class DemoRequest {
        private final String userId;

        /**
         * 构造请求。
         *
         * @param userId 用户 ID
         */
        public DemoRequest(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }
    }
}

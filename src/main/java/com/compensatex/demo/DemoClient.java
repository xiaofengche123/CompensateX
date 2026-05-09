package com.compensatex.demo;

import com.compensatex.autoconfig.EnableCompensateX;
import com.compensatex.demo.DemoService.DemoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo 启动入口，用于演示补偿、幂等和日志效果。
 */
@SpringBootApplication(scanBasePackages = "com.compensatex")
@EnableCompensateX
public class DemoClient implements CommandLineRunner {

    @Autowired
    private DemoService demoService;

    /**
     * 应用启动方法。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(DemoClient.class, args);
    }

    /**
     * 启动后执行演示调用。
     *
     * @param args 启动参数
     */
    @Override
    public void run(String... args) {
        try {
            String first = demoService.createOrder(new DemoRequest("U1001"));
            System.out.println("first result: " + first);
        } catch (Exception ex) {
            System.out.println("first invocation throws: " + ex.getMessage());
        }

        try {
            String second = demoService.createOrder(new DemoRequest("U1001"));
            System.out.println("second result: " + second);
        } catch (Exception ex) {
            System.out.println("second invocation throws: " + ex.getMessage());
        }
    }
}

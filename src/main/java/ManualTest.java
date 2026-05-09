import com.compensatex.demo.DemoClient;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 手工测试入口。
 * <p>
 * 该类用于快速启动 Demo，观察补偿、异步重试、幂等和日志行为。
 */
public class ManualTest {

    /**
     * 启动测试程序。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DemoClient.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread(context::close));
    }
}

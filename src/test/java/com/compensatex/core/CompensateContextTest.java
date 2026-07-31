package com.compensatex.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.compensatex.annotation.Compensable;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class CompensateContextTest {

    @Test
    void preservesConfigurationAndDefensivelyCopiesArguments() throws Exception {
        DummyService service = new DummyService();
        Method method = DummyService.class.getDeclaredMethod("create", String.class);
        Compensable annotation = method.getAnnotation(Compensable.class);
        Object[] originalArgs = {"order-1"};

        CompensateContext context = new CompensateContext(
                service, method, originalArgs, annotation, "user-7");
        originalArgs[0] = "changed";
        Object[] returnedArgs = context.getArgs();
        returnedArgs[0] = "changed-again";

        assertThat(context.getRequestId()).isNotBlank();
        assertThat(context.getTarget()).isSameAs(service);
        assertThat(context.getMethod()).isEqualTo(method);
        assertThat(context.getArgs()).containsExactly("order-1");
        assertThat(context.getCompensable().retryTimes()).isEqualTo(5);
        assertThat(context.getIdempotentValue()).isEqualTo("user-7");
        assertThat(context.getCreateTime()).isNotNull();
    }

    private static class DummyService {

        @Compensable(retryTimes = 5, idempotentKey = "#p0")
        public void create(String orderId) {
            // Test fixture.
        }
    }
}

package com.compensatex.dubbo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dubbo 泛化调用器，用于在补偿场景调用远程 fallback。
 */
public class DubboGenericInvoker {

    private static final Logger log = LoggerFactory.getLogger(DubboGenericInvoker.class);

    private final Map<String, GenericService> genericServiceCache = new ConcurrentHashMap<>();

    /**
     * 执行 Dubbo 泛化调用。
     *
     * @param interfaceName 远程接口全限定名
     * @param methodName    方法名
     * @param parameterTypes 参数类型
     * @param args          方法参数
     * @return 远程调用返回结果
     */
    public Object invoke(String interfaceName, String methodName, Class<?>[] parameterTypes, Object[] args) {
        GenericService genericService = genericServiceCache.computeIfAbsent(interfaceName, this::buildReference);
        String[] parameterTypeNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypeNames[i] = parameterTypes[i].getName();
        }
        log.info("Dubbo generic invoke interface={}, method={}", interfaceName, methodName);
        return genericService.$invoke(methodName, parameterTypeNames, args);
    }

    private GenericService buildReference(String interfaceName) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setInterface(interfaceName);
        reference.setGeneric("true");
        reference.setCheck(false);
        reference.setRetries(0);
        return reference.get();
    }
}

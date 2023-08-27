package com.vincenzoracca.webflux.mdc.util;

import io.micrometer.context.ContextRegistry;
import org.slf4j.MDC;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MDCUtil {

    private MDCUtil() {}

    public static void registerMDC(String mdcKey) {
        Supplier<String> getMDC = () -> MDC.get(mdcKey);
        Consumer<String> putMDC = (value) -> MDC.put(mdcKey, value);
        Runnable removeMDC = () -> MDC.remove(mdcKey);

        ContextRegistry.getInstance()
                .registerThreadLocalAccessor(mdcKey,
                        getMDC,
                        putMDC,
                        removeMDC);
    }
}

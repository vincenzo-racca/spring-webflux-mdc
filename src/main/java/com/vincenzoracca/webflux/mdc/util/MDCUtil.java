package com.vincenzoracca.webflux.mdc.util;

import io.micrometer.context.ContextRegistry;
import org.slf4j.MDC;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MDCUtil {

    private MDCUtil() {}

    public static <T> Mono<T> wrapMDC(Mono<T> mono, Map<String, Object> mdcMap) {
        mdcMap.forEach((key, value) -> registerMDC(key));
        return Mono.defer(() -> mono.contextWrite(Context.of(mdcMap)));
    }

    public static <T> Mono<T> wrapMDC(Mono<T> mono, String mdcKey, String mdcValue) {
        registerMDC(mdcKey);
        return Mono.defer(() -> mono.contextWrite(Context.of(mdcKey, mdcValue)));
    }

    public static <T> Flux<T> wrapMDC(Flux<T> flux, Map<String, Object> mdcMap) {
        mdcMap.forEach((key, value) -> registerMDC(key));
        return Flux.defer(() -> flux.contextWrite(Context.of(mdcMap)));
    }

    public static <T> Flux<T> wrapMDC(Flux<T> flux, String mdcKey, String mdcValue) {
        registerMDC(mdcKey);
        return Flux.defer(() -> flux.contextWrite(Context.of(mdcKey, mdcValue)));
    }

    public static void registerMDC(String mdcKey) {
        Supplier<String> getMDC = () -> MDC.get(mdcKey);
        Consumer<String> putMDC = value -> MDC.put(mdcKey, value);
        Runnable removeMDC = () -> MDC.remove(mdcKey);

        ContextRegistry.getInstance()
                .registerThreadLocalAccessor(mdcKey,
                        getMDC,
                        putMDC,
                        removeMDC);
    }
}

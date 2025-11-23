package com.vincenzoracca.webflux.mdc.util;

import io.micrometer.context.ContextRegistry;
import org.slf4j.MDC;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility class providing helper methods to integrate Spring WebFlux Reactor context
 * with SLF4J's Mapped Diagnostic Context (MDC).
 *
 * <p>This class offers convenient wrappers for {@link Mono} and {@link Flux} that allow
 * attaching MDC key–value pairs to the Reactor {@link Context}. When the wrapped reactive
 * pipeline executes, the contextual values are automatically propagated and synchronized.</p>
 *
 * <p>The {@code wrapMDC} methods ensure that:</p>
 * <ul>
 *     <li>all supplied MDC keys are registered as ThreadLocal accessors via
 *         {@link ContextRegistry};</li>
 *     <li>the provided MDC values are inserted into the Reactor context using
 *         {@code contextWrite};</li>
 *     <li>the MDC values become available to logging frameworks during the lifecycle
 *         of the reactive chain.</li>
 * </ul>
 *
 * <p>This enables consistent, request-scoped logging even in fully reactive,
 * non-blocking execution environments where traditional MDC propagation does not work
 * automatically.</p>
 *
 * <p>The class is non-instantiable and all utility methods are static.</p>
 */
public class MdcUtil {

    private MdcUtil() {}

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

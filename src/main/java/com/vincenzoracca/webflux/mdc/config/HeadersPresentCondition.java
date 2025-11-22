package com.vincenzoracca.webflux.mdc.config;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * {@link Condition} that checks whether the MDC auto-configuration should be enabled.
 *
 * <p>This condition evaluates to {@code true} only if the application has defined
 * at least one property under the prefix {@code spring.mdc.headers}. The purpose
 * is to activate the MDC WebFlux filter and related beans only when the user has
 * explicitly configured one or more header mappings.</p>
 *
 * <p>The check is performed using Spring Boot's {@link Binder}, which attempts to bind
 * {@code spring.mdc.headers} into a {@code Map<String, String>}. If the binding succeeds
 * and the resulting map is non-empty, the condition matches.</p>
 *
 * <p>This condition is used by {@code SpringMdcAutoConfiguration} to prevent unnecessary
 * bean creation when the MDC library is present on the classpath but not configured.</p>
 */
public class HeadersPresentCondition implements Condition {
    @Override
    public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata metadata) {
        Binder binder = Binder.get(ctx.getEnvironment());
        return binder.bind("spring.mdc.headers", Bindable.mapOf(String.class, String.class))
                .map(Map::isEmpty)
                .map(isEmpty -> !isEmpty)
                .orElse(false);
    }
}

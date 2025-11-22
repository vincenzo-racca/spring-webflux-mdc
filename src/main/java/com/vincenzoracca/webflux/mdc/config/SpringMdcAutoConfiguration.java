package com.vincenzoracca.webflux.mdc.config;

import com.vincenzoracca.webflux.mdc.filter.MdcFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Auto-configuration for the WebFlux MDC support.
 *
 * <p>This configuration is automatically applied when the library is on the classpath
 * and at least one property under {@code spring.mdc.headers} is defined in the
 * application's configuration files.</p>
 *
 * <p>No manual registration (such as {@code @Import}) is required.</p>
 *
 * <p>When activated, this auto-configuration registers:</p>
 * <ul>
 *     <li>{@link SpringMDCProperties} – configuration properties bound from {@code spring.mdc.*}</li>
 *     <li>{@link MdcRegistry} – registry used to store MDC settings</li>
 *     <li>{@link MdcFilter} – a WebFlux {@code WebFilter} with highest precedence
 *         that injects MDC values based on configured headers</li>
 * </ul>
 *
 * <p>The configuration is enabled only if {@link HeadersPresentCondition} detects
 * that {@code spring.mdc.headers} contains at least one entry.</p>
 *
 * @author Vincenzo Racca
 */
@AutoConfiguration
@EnableConfigurationProperties
@Conditional(HeadersPresentCondition.class)
public class SpringMdcAutoConfiguration {

    @Bean
    SpringMDCProperties springMDCProperties() {
        return new SpringMDCProperties();
    }

    @Bean
    MdcRegistry mdcRegistry(SpringMDCProperties mdcProperties) {
        return new MdcRegistry(mdcProperties);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    MdcFilter mdcFilter(SpringMDCProperties mdcProperties) {
        return new MdcFilter(mdcProperties);
    }

}

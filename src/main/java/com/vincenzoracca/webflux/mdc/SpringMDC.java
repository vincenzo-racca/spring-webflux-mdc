package com.vincenzoracca.webflux.mdc;

import com.vincenzoracca.webflux.mdc.config.MDCRegistry;
import com.vincenzoracca.webflux.mdc.config.SpringMDCProperties;
import com.vincenzoracca.webflux.mdc.filter.MDCFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Vincenzo Racca
 *
 * If you want to use this library, you need to import this configuration class into your Spring application like this:
 * @Import(SpringMDC.class)
 *
 */
@Configuration
@EnableConfigurationProperties
public class SpringMDC {

    @Bean
    SpringMDCProperties springMDCProperties() {
        return new SpringMDCProperties();
    }

    @Bean
    MDCRegistry mdcRegistry(SpringMDCProperties mdcProperties) {
        return new MDCRegistry(mdcProperties);
    }

    @Bean
    MDCFilter mdcFilter(SpringMDCProperties mdcProperties) {
        return new MDCFilter(mdcProperties);
    }

}

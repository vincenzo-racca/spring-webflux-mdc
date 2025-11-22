package com.vincenzoracca.webflux.mdc.config;

import com.vincenzoracca.webflux.mdc.util.MdcUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Hooks;

/**
 * @author Vincenzo Racca
 *
 * This class automatically handles the addition and removal of variables in the MDC. In particular,
 * it handles all the keys of the headers variable in @{@link SpringMDCProperties}
 */
@RequiredArgsConstructor
public class MdcRegistry {

    private final SpringMDCProperties mdcProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        Hooks.enableAutomaticContextPropagation();

        mdcProperties.getHeaders().forEach((headerKey, headerValue) -> MdcUtil.registerMDC(headerValue));

    }
}

package com.vincenzoracca.webflux.mdc.filter;

import com.vincenzoracca.webflux.mdc.config.SpringMDCProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Vincenzo Racca
 *
 * This WebFilter checks the request headers and add the MDC keys.
 */
@Component
@RequiredArgsConstructor
public class MDCFilter implements WebFilter {


    private final SpringMDCProperties mdcProperties;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Map<String, Object> mapInContext = new HashMap<>();

        if(! mdcProperties.getHeaders().isEmpty()) {
            HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
            Map<String, String> headersForMDC = mdcProperties.getHeaders();
            List<String> defaults = mdcProperties.getDefaults();

            headersForMDC.forEach((headerForMDCKey, headerForMDCValue) -> {
                if(requestHeaders.containsKey(headerForMDCKey)) {
                    mapInContext.put(headerForMDCValue, requestHeaders.get(headerForMDCKey).get(0));
                }
                else if(defaults.contains(headerForMDCKey)) {
                    mapInContext.put(headerForMDCValue, UUID.randomUUID().toString());
                }
            });
        }

        if(! mapInContext.isEmpty()) {
            return Mono.defer(() -> chain.filter(exchange)).contextWrite(Context.of(mapInContext));
        }
        else {
            return chain.filter(exchange);
        }
    }
}

package com.vincenzoracca.webflux.mdc.filter;

import com.vincenzoracca.webflux.mdc.config.SpringMDCProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
 * Reactive {@link WebFilter} that enriches Reactor's {@link Context} with MDC key–value
 * pairs derived from incoming HTTP request headers.
 *
 * <p>This filter inspects the request headers and, based on the configuration provided
 * through {@link SpringMDCProperties}, extracts values to be propagated within the
 * Reactor execution context. These values can then be accessed by logging frameworks
 * supporting MDC (Mapped Diagnostic Context), enabling consistent, request-scoped
 * logging throughout the reactive pipeline.</p>
 *
 * <p>For each entry defined under {@code spring.mdc.headers}, the filter attempts
 * to read the corresponding HTTP header from the incoming request:</p>
 *
 * <ul>
 *     <li>If the header is present, the mapped MDC key is populated with its value.</li>
 *     <li>If the header is missing but is listed under {@link SpringMDCProperties#getDefaults()},
 *         a new random UUID is generated and assigned as its value.</li>
 * </ul>
 *
 * <p>If at least one MDC value is resolved, the filter inserts the resulting map into
 * the Reactor {@link Context} using {@code contextWrite}, ensuring propagation across the
 * entire reactive chain.</p>
 *
 * <p>This filter is typically registered automatically via the library's
 * auto-configuration and ordered with highest precedence to ensure MDC values are
 * established before any user-defined filters execute.</p>
 *
 * @author Vincenzo Racca
 */
@RequiredArgsConstructor
public class MdcFilter implements WebFilter {


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

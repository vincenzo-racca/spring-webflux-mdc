package com.vincenzoracca.webflux.mdc.api;

import com.vincenzoracca.webflux.mdc.api.model.MessageResponse;
import com.vincenzoracca.webflux.mdc.util.MdcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@RestController
public class MockApi {

    private static final Logger log = LoggerFactory.getLogger(MockApi.class);

    @GetMapping("test-client")
    public Mono<ResponseEntity<MessageResponse>> getMDCExample(@RequestHeader("X-Amzn-Trace-Id") String awsTraceId) {
        log.info("[{}] Called getMDCExample with header:", awsTraceId);
        return Mono.just("test-product")
                .delayElement(Duration.ofMillis(1))
                .flatMap(product ->
                        Flux.concat(
                                        addProduct(product, awsTraceId),
                                        notifyShop(product, awsTraceId))
                                .then(Mono.just(ResponseEntity.ok(new MessageResponse("Hello World!")))));
    }

    @GetMapping("test-client-programmatically")
    public Mono<ResponseEntity<MessageResponse>> getMDCProgrammaticallyExampleOne(@RequestHeader("an-header-not-registered") String anHeader) {
        log.info("[{}] Called getMDCExample with header but without MDC because it is not wrapped:", anHeader);
        Mono<ResponseEntity<MessageResponse>> responseEntityMono = Mono.just("test-another-product")
                .delayElement(Duration.ofMillis(1))
                .flatMap(product ->
                        Flux.concat(
                                        addProduct(product, anHeader),
                                        notifyShop(product, anHeader))
                                .then(Mono.just(ResponseEntity.ok(new MessageResponse("Hello World!")))));

        return MdcUtil.wrapMDC(responseEntityMono, Map.of("my-mdc-key", anHeader));
    }

    @GetMapping("test-client-programmatically-2")
    public Mono<ResponseEntity<MessageResponse>> getMDCProgrammaticallyExampleTwo(@RequestHeader("an-header-not-registered") String anHeader) {
        log.info("[{}] Called getMDCExample with header but without MDC because it is not wrapped:", anHeader);
        Mono<ResponseEntity<MessageResponse>> responseEntityMono = Mono.just("test-another-product")
                .delayElement(Duration.ofMillis(1))
                .flatMap(product ->
                        Flux.concat(
                                        addProduct(product, anHeader),
                                        notifyShop(product, anHeader))
                                .then(Mono.just(ResponseEntity.ok(new MessageResponse("Hello World!")))));

        return MdcUtil.wrapMDC(responseEntityMono, "my-mdc-key", anHeader);
    }

    Mono<Void> addProduct(String productName, String awsTraceId) {
        log.info("Adding product: {}, with header: {}", productName, awsTraceId);
        return Mono.empty(); // Assume we’re actually storing the product
    }

    Mono<Boolean> notifyShop(String productName, String awsTraceId) {
        log.info("Notifying shop about: {} with header: {}", productName, awsTraceId);
        return Mono.just(true);
    }
}

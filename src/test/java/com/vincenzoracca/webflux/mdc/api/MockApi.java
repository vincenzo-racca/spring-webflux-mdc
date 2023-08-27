package com.vincenzoracca.webflux.mdc.api;

import com.vincenzoracca.webflux.mdc.api.model.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@Slf4j
public class MockApi {

    @GetMapping("test-client")
    public Mono<ResponseEntity<MessageResponse>> getMDCExample(@RequestHeader("X-Amzn-Trace-Id") String awsTraceId) {
        log.info("[{}] Called getMDCExample with header:", awsTraceId);
        return Mono.just("test-product")
                .delayElement(Duration.ofMillis(1))// <3>
                .flatMap(product ->
                        Flux.concat(
                                        addProduct(product, awsTraceId), // <4>
                                        notifyShop(product, awsTraceId))
                                .then(Mono.just(ResponseEntity.ok(new MessageResponse("Hello World!")))));
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

package com.vincenzoracca.webflux.mdc.api;

import com.vincenzoracca.webflux.mdc.api.model.DocumentMetadata;
import com.vincenzoracca.webflux.mdc.api.model.MessageResponse;
import com.vincenzoracca.webflux.mdc.api.repo.DocumentMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MongoApi {

    private final DocumentMetadataRepository documentMetadataRepository;

    @GetMapping("test-client-mongodb")
    public Mono<ResponseEntity<MessageResponse>> getMDCExampleProblem(@RequestHeader("X-Amzn-Trace-Id") String awsTraceId,
                                                                      @RequestParam String fileKey) {
        log.info("[{}] Called getMDCExample with header:", awsTraceId);
        return Mono.just("test-product")
                .delayElement(Duration.ofMillis(1))
                .flatMap(x -> getImage(fileKey))
                .then(Mono.just(ResponseEntity.ok(new MessageResponse("Hello World!"))));
    }

    public Mono<String> getImage(String fileKey) {
        // Has MDC
        log.info("Inside getImage()");

        return getDocumentMetadata(fileKey)
                .flatMap(this::downloadImage);
    }

    private Mono<DocumentMetadata> getDocumentMetadata(String fileKey) {
        // Has MDC
        log.info("Inside getDocumentMetadata()");
        return documentMetadataRepository.findById(fileKey)
                .doOnNext(documentMetadata -> log.info("Document found: {}", documentMetadata));
    }

    private Mono<String> downloadImage(DocumentMetadata documentMetadata) {
        // No MDC
        log.info("Inside downloadImage()");
        return Mono.just("example");
    }
}

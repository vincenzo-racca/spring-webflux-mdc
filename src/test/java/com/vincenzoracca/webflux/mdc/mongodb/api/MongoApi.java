package com.vincenzoracca.webflux.mdc.mongodb.api;

import com.vincenzoracca.webflux.mdc.model.MessageResponse;
import com.vincenzoracca.webflux.mdc.mongodb.model.DocumentMetadata;
import com.vincenzoracca.webflux.mdc.mongodb.repo.DocumentMetadataRepository;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class MongoApi {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MongoApi.class);

    private final DocumentMetadataRepository documentMetadataRepository;

    public MongoApi(DocumentMetadataRepository documentMetadataRepository) {
        this.documentMetadataRepository = documentMetadataRepository;
    }

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
        log.info("Inside getImage()");

        return getDocumentMetadata(fileKey)
                .flatMap(this::downloadImage);
    }

    private Mono<DocumentMetadata> getDocumentMetadata(String fileKey) {
        log.info("Inside getDocumentMetadata()");
        return documentMetadataRepository.findById(fileKey)
                .doOnNext(documentMetadata -> log.info("Document found: {}", documentMetadata));
    }

    private Mono<String> downloadImage(DocumentMetadata documentMetadata) {
        log.info("Inside downloadImage()");
        return Mono.just("example");
    }
}

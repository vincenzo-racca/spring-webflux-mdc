package com.vincenzoracca.webflux.mdc.api.repo;

import com.vincenzoracca.webflux.mdc.api.model.DocumentMetadata;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DocumentMetadataRepository extends ReactiveMongoRepository<DocumentMetadata, String> {
}

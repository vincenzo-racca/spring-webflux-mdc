package com.vincenzoracca.webflux.mdc.mongodb.repo;

import com.vincenzoracca.webflux.mdc.mongodb.model.DocumentMetadata;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DocumentMetadataRepository extends ReactiveMongoRepository<DocumentMetadata, String> {}

package com.vincenzoracca.webflux.mdc.mongodb.repo;

import com.vincenzoracca.webflux.mdc.mongodb.model.DocumentMetadata;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface DocumentMetadataRepository extends R2dbcRepository<DocumentMetadata, String> {}

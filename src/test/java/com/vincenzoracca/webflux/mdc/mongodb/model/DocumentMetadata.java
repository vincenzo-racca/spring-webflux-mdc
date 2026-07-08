package com.vincenzoracca.webflux.mdc.mongodb.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public record DocumentMetadata(@Id String fileKey) {}

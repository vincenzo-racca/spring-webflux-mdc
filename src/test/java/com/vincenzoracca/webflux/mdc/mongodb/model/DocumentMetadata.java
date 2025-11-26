package com.vincenzoracca.webflux.mdc.mongodb.model;

import org.springframework.data.annotation.Id;

public record DocumentMetadata(@Id String fileKey) {}

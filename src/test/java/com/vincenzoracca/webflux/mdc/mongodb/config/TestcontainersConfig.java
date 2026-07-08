package com.vincenzoracca.webflux.mdc.mongodb.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestcontainersConfig {

    static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0.26"));


    static {
        mongoDBContainer.start();
        var mappedPort = mongoDBContainer.getMappedPort(27017);
        System.setProperty("mongodb.container.port", String.valueOf(mappedPort));
    }


}

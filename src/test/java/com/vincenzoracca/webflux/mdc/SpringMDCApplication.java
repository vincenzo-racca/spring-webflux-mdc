package com.vincenzoracca.webflux.mdc;

import com.vincenzoracca.webflux.mdc.api.config.TestcontainersConfig;
import com.vincenzoracca.webflux.mdc.api.model.DocumentMetadata;
import com.vincenzoracca.webflux.mdc.api.repo.DocumentMetadataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({SpringMDC.class, TestcontainersConfig.class})
public class SpringMDCApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMDCApplication.class);
    }

    @Bean
    CommandLineRunner insertData(DocumentMetadataRepository repository) {
        return args -> repository.save(new DocumentMetadata("fileKey1")).block();
    }
}

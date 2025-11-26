package com.vincenzoracca.webflux.mdc;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.vincenzoracca.webflux.mdc.mongodb.config.TestcontainersConfig;
import com.vincenzoracca.webflux.mdc.mongodb.model.DocumentMetadata;
import com.vincenzoracca.webflux.mdc.mongodb.repo.DocumentMetadataRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "spring.data.mongodb.database=OAuth2Sample",
        "spring.data.mongodb.port=${mongodb.container.port}",
        "spring.data.mongodb.host=localhost",
        "spring.data.mongodb.auto-index-creation=true"
})
@Import({TestcontainersConfig.class})
class MongoApiMdcTestIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;


    private ListAppender<ILoggingEvent> listAppender;
    private Logger mongoApiLogger;

    @BeforeEach
    void setupAppender() {
        databaseClient.sql("""
                CREATE TABLE IF NOT EXISTS document_metadata (
                    file_key VARCHAR(255) NOT NULL,
                    PRIMARY KEY (file_key)
                )
                """)
                .fetch()
                .rowsUpdated()
                .block();

        mongoApiLogger = (Logger) LoggerFactory.getLogger("com.vincenzoracca.webflux.mdc.mongodb.api.MongoApi");
        listAppender = new ListAppender<>();
        listAppender.start();
        mongoApiLogger.addAppender(listAppender);
    }

    @AfterEach
    void teardownAppender() {
        if (mongoApiLogger != null) {
            mongoApiLogger.detachAppender(listAppender);
        }
    }

    @Test
    void allLogsShouldContainMdcTraceId() {
        // dato
        String fileKey = "file-123";
        String awsTraceId = "aws-TRACE-XYZ-123";

        // prepariamo il repository mockato: ritorna un DocumentMetadata qualunque
        DocumentMetadata metadata = new DocumentMetadata(fileKey);

        databaseClient.sql("INSERT INTO document_metadata (file_key) VALUES (:fileKey)")
                .bind("fileKey", metadata.fileKey())
                .fetch()
                .rowsUpdated()
                .block();

        // quando x
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/test-client-mongodb")
                        .queryParam("fileKey", fileKey)
                        .build())
                .header("X-Amzn-Trace-Id", awsTraceId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        // allora: raccogliamo gli eventi di log catturati
        List<ILoggingEvent> events = listAppender.list;
        assertThat(events).isNotEmpty();

        // controlliamo che ogni evento abbia la mappa MDC con chiave "trace_id" e valore awsTraceId
        for (ILoggingEvent e : events) {
            Map<String, String> mdc = e.getMDCPropertyMap();
            assertThat(mdc)
                    .withFailMessage(() -> "Missing MDC trace_id in log event: " + e.getFormattedMessage())
                    .containsEntry("trace_id", awsTraceId);
        }
    }
}

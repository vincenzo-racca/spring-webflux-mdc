package com.vincenzoracca.webflux.mdc;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.vincenzoracca.webflux.mdc.api.MockApi;
import com.vincenzoracca.webflux.mdc.config.SpringMdcAutoConfiguration;
import com.vincenzoracca.webflux.mdc.model.MessageResponse;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(MockApi.class)
@Import(SpringMdcAutoConfiguration.class)
class SpringMdcTests {

    private static final String FILE_SOURCE_LOG = "src/test/resources/tests.log";


    @Autowired
    private WebTestClient webTestClient;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger mockApiLogger;

    @BeforeEach
    void setupAppender() {
        mockApiLogger = (Logger) LoggerFactory.getLogger("com.vincenzoracca.webflux.mdc.api.MockApi");
        listAppender = new ListAppender<>();
        listAppender.start();
        mockApiLogger.addAppender(listAppender);
    }

    @AfterEach
    void teardownAppender() {
        if (mockApiLogger != null) {
            mockApiLogger.detachAppender(listAppender);
        }
    }


    @Test
    void testGetMDCExampleTest() {
        String traceId = "sample-trace-id";

        webTestClient.get()
                .uri("/test-client")
                .header("X-Amzn-Trace-Id", traceId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MessageResponse.class);

        List<ILoggingEvent> events = listAppender.list;
        assertThat(events).isNotEmpty();

        for (ILoggingEvent e : events) {
            Map<String, String> mdc = e.getMDCPropertyMap();
            assertThat(mdc)
                    .withFailMessage(() -> "Missing MDC trace_id in log event: " + e.getFormattedMessage())
                    .containsEntry("trace_id", traceId);
        }
    }

    @Test
    void testGetMDCProgrammaticallyExampleOneTest() {
        webTestClient.get()
                .uri("/test-client-programmatically")
                .header("an-header-not-registered", "a-value")
                .exchange()
                .expectStatus().isOk()
                .expectBody(MessageResponse.class);

        List<ILoggingEvent> events = listAppender.list;
        assertThat(events).isNotEmpty();
        events.remove(0);

        for (ILoggingEvent e : events) {
            Map<String, String> mdc = e.getMDCPropertyMap();
            assertThat(mdc)
                    .withFailMessage(() -> "Missing MDC trace_id in log event: " + e.getFormattedMessage())
                    .containsEntry("my-mdc-key", "a-value");
        }

    }

    @Test
    void testGetMDCProgrammaticallyExampleTwoTest() {
        webTestClient.get()
                .uri("/test-client-programmatically-2")
                .header("an-header-not-registered", "a-value")
                .exchange()
                .expectStatus().isOk()
                .expectBody(MessageResponse.class);

        List<ILoggingEvent> events = listAppender.list;
        assertThat(events).isNotEmpty();
        events.remove(0);

        for (ILoggingEvent e : events) {
            Map<String, String> mdc = e.getMDCPropertyMap();
            assertThat(mdc)
                    .withFailMessage(() -> "Missing MDC trace_id in log event: " + e.getFormattedMessage())
                    .containsEntry("my-mdc-key", "a-value");
        }
    }

    @BeforeEach
    @AfterEach
    void clean() throws IOException {
        FileUtils.write(new File(FILE_SOURCE_LOG), "", StandardCharsets.UTF_8);
    }
}

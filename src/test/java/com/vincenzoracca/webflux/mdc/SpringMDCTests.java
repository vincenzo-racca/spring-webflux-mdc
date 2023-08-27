package com.vincenzoracca.webflux.mdc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vincenzoracca.webflux.mdc.api.model.MessageResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringMDCTests {

    private static final String FILE_SOURCE_LOG = "src/test/resources/tests.log";

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void testGetMDCExample() throws IOException {
        String traceId = "sample-trace-id";

        webTestClient.get()
                .uri("http://localhost:" + port + "/prova")
                .header("X-Amzn-Trace-Id", traceId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MessageResponse.class)
                .consumeWith(result -> {
                    MessageResponse response = result.getResponseBody();
                    // Verifica qui i contenuti della risposta se necessario
                });

        List<Map> jsonArray = retrieveJsonArrayFromFile();

        jsonArray.forEach(json -> {
            assertThat(json).containsKey("trace_id");
            assertThat(json.get("trace_id")).isEqualTo("sample-trace-id");
        });
    }

    private List<Map> retrieveJsonArrayFromFile() {
        try {
            FileInputStream fis = new FileInputStream(FILE_SOURCE_LOG);
            String data = IOUtils.toString(fis, StandardCharsets.UTF_8);
            String[] split = data.split("\n");
            return Arrays.stream(split).map(s -> {
                        try {
                            return objectMapper.readValue(s, Map.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (Exception e) {
            System.err.println("ERROR RETRIEVING FILE");
            throw new RuntimeException(e);
        }

    }

    @AfterEach
    public void clean() throws IOException {
        FileUtils.write(new File(FILE_SOURCE_LOG), "", StandardCharsets.UTF_8);
    }
}

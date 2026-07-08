# Spring WebFlux MDC

Utility library that propagates SLF4J MDC values across reactive Spring WebFlux pipelines.

In a reactive application, execution can move across different threads and the traditional MDC `ThreadLocal` state is not enough. This library stores request-scoped values in the Reactor `Context`, registers them with Micrometer Context Propagation, and makes them available to logs emitted inside the reactive chain.

## Features

- Automatic Spring Boot 3 and 4 auto-configuration.
- WebFlux `WebFilter` with highest precedence.
- MDC values populated from HTTP request headers.
- Optional UUID fallback when a configured header is missing.
- Programmatic MDC wrapping for `Mono` and `Flux`.
- Automatic Reactor context propagation via `Hooks.enableAutomaticContextPropagation()`.

## Requirements

- Java 17+
- Spring Boot 3.x or 4.x
- Spring WebFlux
- SLF4J-compatible logging backend

## Compatibility

| spring-webflux-mdc version | Spring Boot compatibility |
| --- | --- |
| 1.1.0 | 3.x |
| 1.2.0 | 3.x, 4.x |

## Installation

### Maven

```xml
<dependency>
    <groupId>com.vincenzoracca</groupId>
    <artifactId>spring-webflux-mdc</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation 'com.vincenzoracca:spring-webflux-mdc:1.1.0'
}
```

## How it works

When the library is on the classpath, `SpringMdcAutoConfiguration` is activated only if at least one property under `spring.mdc.headers` is configured.

The auto-configuration registers:

| Bean | Purpose |
| --- | --- |
| `SpringMDCProperties` | Binds properties under `spring.mdc.*`. |
| `MdcRegistry` | Registers configured MDC keys and enables automatic context propagation when the application is ready. |
| `MdcFilter` | Reads configured headers from incoming WebFlux requests and writes the resolved MDC values into the Reactor `Context`. |

If `spring.mdc.headers` is empty or missing, the auto-configuration is not applied and no MDC filter is registered.

## Configuration

Add the header-to-MDC mapping to your `application.properties` or `application.yml`.

```properties
spring.mdc.headers.<header-name>=<mdc-key>
```

Example:

```properties
spring.mdc.headers.X-Amzn-Trace-Id=trace_id
```

With this configuration, every incoming request containing the `X-Amzn-Trace-Id` header gets an MDC entry named `trace_id` with the header value.

### Default values

You can ask the library to generate a UUID when a configured header is missing:

```properties
spring.mdc.headers.X-Amzn-Trace-Id=trace_id
spring.mdc.defaults=X-Amzn-Trace-Id
```

`spring.mdc.defaults` is a comma-separated list of HTTP header names. Each listed header must also be present in `spring.mdc.headers`, because the mapped value is used as the MDC key.

### Multiple headers

```properties
spring.mdc.headers.X-Amzn-Trace-Id=trace_id
spring.mdc.headers.X-Correlation-Id=correlation_id
spring.mdc.headers.X-Request-Id=request_id
spring.mdc.defaults=X-Correlation-Id,X-Request-Id
```

Notes:

- The property key is the HTTP header name.
- The property value is the MDC key.
- If a request header has multiple values, the first value is used.
- Defaults are generated only for headers listed in `spring.mdc.defaults`.

See the runnable test configuration in [`src/test/resources/application.properties`](./src/test/resources/application.properties).

## Logging configuration

The library makes MDC values available to your logging backend. Configure your logger layout/encoder to include MDC fields.

For example, with `logstash-logback-encoder`:

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>

    <root level="info">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

The test application contains a complete example in [`src/test/resources/logback-logstash.xml`](./src/test/resources/logback-logstash.xml).

## Usage with request headers

After configuring the header mapping, no extra code is required in your controllers.

```java
@GetMapping("test-client")
public Mono<ResponseEntity<MessageResponse>> getMDCExample(
        @RequestHeader("X-Amzn-Trace-Id") String traceId) {

    log.info("Received request");

    return Mono.just("test-product")
            .flatMap(product -> {
                log.info("Processing product: {}", product);
                return Mono.just(ResponseEntity.ok(new MessageResponse("Hello World!")));
            });
}
```

If the request contains:

```http
X-Amzn-Trace-Id: sample-trace-id
```

logs emitted inside the reactive chain contain:

```json
{
  "trace_id": "sample-trace-id"
}
```

## Programmatic MDC

If an MDC value does not come from an HTTP header, wrap the outermost `Mono` or `Flux` with `MdcUtil`.

### Single key

```java
import com.vincenzoracca.webflux.mdc.util.MdcUtil;

@GetMapping("test-client-programmatically")
public Mono<ResponseEntity<MessageResponse>> getMDCProgrammatically(
        @RequestHeader("an-header-not-registered") String headerValue) {

    Mono<ResponseEntity<MessageResponse>> response = Mono.just("test-product")
            .flatMap(product -> {
                log.info("Processing product: {}", product);
                return Mono.just(ResponseEntity.ok(new MessageResponse("Hello World!")));
            });

    return MdcUtil.wrapMDC(response, "my-mdc-key", headerValue);
}
```

### Multiple keys

```java
return MdcUtil.wrapMDC(response, Map.of(
        "correlation_id", correlationId,
        "tenant_id", tenantId
));
```

### Flux

```java
Flux<Event> events = eventService.streamEvents();

return MdcUtil.wrapMDC(events, "correlation_id", correlationId);
```

`MdcUtil.wrapMDC(...)` registers the MDC keys and writes the values into the Reactor `Context`, so they are propagated while the wrapped reactive pipeline executes.

## Manual test application

The repository includes a small WebFlux test application.

1. Run [`SpringMdcApplication`](./src/test/java/com/vincenzoracca/webflux/mdc/SpringMdcApplication.java) from your IDE.
2. Send a request:

   ```bash
   curl -H "X-Amzn-Trace-Id: sample-trace-id" http://localhost:8080/test-client
   ```

3. Check the JSON logs. The `trace_id` field should contain `sample-trace-id`.

You can also run [`src/test/resources/test-client.sh`](./src/test/resources/test-client.sh), which executes 100 requests using HTTPie.

## Running tests

```bash
./mvnw test
```

Run the full Maven verification lifecycle, including integration tests:

```bash
./mvnw verify
```

The integration test [`MongoApiMdcTestIT`](./src/test/java/com/vincenzoracca/webflux/mdc/MongoApiMdcTestIT.java) uses Testcontainers with MongoDB.

## License

This project is licensed under the [Apache License 2.0](./LICENSE.md).

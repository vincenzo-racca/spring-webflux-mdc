## Spring Webflux MDC

This library is a utility for adding MDC logs to a Spring Webflux application (with Spring Boot version 3).

### Import the library

With Maven:
```xml
<dependency>
   <groupId>com.vincenzoracca</groupId>
   <artifactId>spring-webflux-mdc</artifactId>
   <version>1.1.0</version>
</dependency>
```
or with Gradle:
```json
dependencies {
	implementation 'com.vincenzoracca:spring-webflux-mdc:1.1.0'
}
```

### Usage
In your Spring WebFlux Application: 
1. Ensure that your project includes the WebFlux MDC library as a dependency.
   No manual import or @Configuration is needed: the library provides an auto-configuration class 
   (SpringMdcAutoConfiguration) that is automatically applied only if at least one property under spring.mdc.headers is 
   defined, thanks to the HeadersPresentCondition.
   ```
2. add in your application.properties the headers keys and the relatives mdc keys in this way:
   ```
   spring.mdc.headers.<header_key_1>=<mdc_key_1> 
   spring.mdc.headers.<header_key_2>=<mdc_key_2>
   ```
3. Optionally, if the HTTP request headers don't contain the <header_key>, the library can add the related MDC with a 
   UUID value. If you want me to do this, in your application.properties add this:
   ```
   spring.mdc.defaults=<header_key_1>,<header_key_2>
   ```
   The <b>spring.mdc.defaults</b> property accepts a list of string (the header keys), with the comma as the delimiter.

See the example in [application.properties](./src/test/resources/application.properties)

### Usage with programmatically MDC (without headers)
If you need to add a MDC key programmatically, in the most "external" method that contains the MDC key:
1. wrap the method using the wrapMDC method of [MdcUtil](./src/main/java/com/vincenzoracca/webflux/mdc/util/MdcUtil.java)
Example: \
Original method:
```java
@GetMapping("test-client-programmatically-2")
public Mono<ResponseEntity<MessageResponse>> getMDCProgrammaticallyExampleTwo(@RequestHeader("an-header-not-registered") String anHeader) {
  log.info("[{}] Called getMDCExample with header but without MDC because it is not wrapped:", anHeader);
  return Mono.just("test-another-product")
          .delayElement(Duration.ofMillis(1))
          .flatMap(product ->
                  Flux.concat(
                                  addProduct(product, anHeader),
                                  notifyShop(product, anHeader))
                          .then(Mono.just(ResponseEntity.ok(new MessageResponse("Hello World!")))));

}
```

With MDC:
```java
@GetMapping("test-client-programmatically-2")
public Mono<ResponseEntity<MessageResponse>> getMDCProgrammaticallyExampleTwo(@RequestHeader("an-header-not-registered") String anHeader) {
  log.info("[{}] Called getMDCExample with header but without MDC because it is not wrapped:", anHeader);
  Mono<ResponseEntity<MessageResponse>> responseEntityMono = Mono.just("test-another-product")
          .delayElement(Duration.ofMillis(1))
          .flatMap(product ->
                  Flux.concat(
                                  addProduct(product, anHeader),
                                  notifyShop(product, anHeader))
                          .then(Mono.just(ResponseEntity.ok(new MessageResponse("Hello World!")))));

  return MDCUtil.wrapMDC(responseEntityMono, "my-mdc-key", anHeader);
}
```

If you need to pass more MDC keys, you can use the wrapMDC method that accepts a Map of <mdc_key, mdc_value>.

### Test with an HTTP client manually with the IDE
1. Run the [SpringMDCApplication](./src/test/java/com/vincenzoracca/webflux/mdc/SpringMDCApplication.java) main class from your IDE
2. Run the [test-client.sh](./src/test/resources/test-client.sh) script to execute 100 HTTP calls or
   make an HTTP call manually with the `localhost:8080/test-client` endpoint with the `X-Amzn-Trace-Id` request header
3. Watch the logs in the [tests.log](./src/test/resources/tests.log) file
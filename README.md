## Spring Webflux MDC

This library is a utility for adding MDC logs to a Spring Webflux application (with Spring Boot version 3).

### Import the library

With Maven:
```xml
<dependency>
   <groupId>com.vincenzoracca</groupId>
   <artifactId>spring-webflux-mdc</artifactId>
   <version>1.0.2</version>
</dependency>
```
or with Gradle:
```json
dependencies {
	implementation 'com.vincenzoracca:spring-webflux-mdc:1.0.2'
}
```

### Usage
In your Spring WebFlux Application: 
1. import the [SpringMDC](./src/main/java/com/vincenzoracca/webflux/mdc/SpringMDC.java) class in a `@Configuration` class
   or in the main class (`@SpringBootApplication`), in this way:
   ```java
   @Configuration
   @Import(SpringMDC.class)
   public class AppConfig {
   }
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
If you need to add a MDC key programmatically, in the most "external" method:
1. register the MDC key using the [SpringMDC](./src/main/java/com/vincenzoracca/webflux/mdc/util/MDCUtil.java) class
2. in your Reactor chain, add the contextWrite method with MDC key and MDC value in this way: \
   `.contextWrite(Context.of(<mdc_key>, <mdc_value>))`

### Test with an HTTP client manually with the IDE
1. Run the [SpringMDCApplication](./src/test/java/com/vincenzoracca/webflux/mdc//SpringMDCApplication.java) main class from your IDE
2. Run the [test-client.sh](./src/test/resources/test-client.sh) script to execute 100 HTTP calls or
   make an HTTP call manually with the `localhost:8080/test-client` endpoint with the `X-Amzn-Trace-Id` request header
3. Watch the logs in the [tests.log](./src/test/resources/tests.log) file
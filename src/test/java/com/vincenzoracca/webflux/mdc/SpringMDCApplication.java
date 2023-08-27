package com.vincenzoracca.webflux.mdc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SpringMDC.class)
public class SpringMDCApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMDCApplication.class);
    }
}

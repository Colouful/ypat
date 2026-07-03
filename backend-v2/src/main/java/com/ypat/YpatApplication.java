package com.ypat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * backend-v2 entry point.
 *
 * PR-04 only. This is the smallest possible Spring Boot 2.7.18 +
 * Java 17 application that boots. It deliberately:
 *
 *   - Has zero business endpoints (no Controller, no Service).
 *   - Has zero persistence (no DataSource, no Flyway).
 *   - Has zero Spring Modulith (that lands in PR-12).
 *   - Has zero springdoc-openapi (that lands once a Controller exists).
 *
 * The first real endpoint arrives with PR-06 (package skeleton) or
 * PR-11 (read-only work migration) — whichever is reviewed first.
 */
@SpringBootApplication
public class YpatApplication {

    public static void main(String[] args) {
        SpringApplication.run(YpatApplication.class, args);
    }
}
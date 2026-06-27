package com.zycus.ziprun.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Customises Flyway startup so it runs AFTER Hibernate has finished
 * creating the schema via ddl-auto=create-drop.
 *
 * Without this, Flyway and Hibernate race each other on startup —
 * Flyway tries to insert seed data before the tables exist.
 *
 * The FlywayMigrationStrategy bean gives us full control over when
 * Flyway.migrate() is called. Spring Boot calls this strategy bean
 * instead of running Flyway automatically during datasource init.
 * By the time this bean method executes, the JPA EntityManagerFactory
 * is already initialised, meaning Hibernate has already run DDL.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return Flyway::migrate;
    }
}

package com.zycus.ziprun.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Breaks the circular dependency between Flyway and entityManagerFactory.
 *
 * Spring Boot's FlywayAutoConfiguration makes JPA depend on Flyway
 * (so Flyway runs before Hibernate). But we need the opposite — Hibernate
 * creates the schema first (create-drop), then Flyway seeds data.
 *
 * Solution: disable FlywayAutoConfiguration, define our own Flyway bean
 * that explicitly depends on EntityManagerFactory. Spring will then
 * initialise JPA first, then our Flyway bean, then call migrate().
 */
@Configuration
@EnableConfigurationProperties(FlywayProperties.class)
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource, EntityManagerFactory entityManagerFactory) {
        // EntityManagerFactory parameter forces Spring to initialise JPA first.
        // Hibernate runs create-drop DDL during EMF initialisation,
        // so by the time this bean is created the tables already exist.
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
    }
}
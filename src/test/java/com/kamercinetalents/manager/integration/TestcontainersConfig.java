package com.kamercinetalents.manager.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

/**
 * Configuration Testcontainers — démarre un conteneur PostgreSQL 16 éphémère
 * pour les tests d'intégration. Les migrations Flyway V1-V8 sont appliquées
 * automatiquement par Spring Boot sur le conteneur.
 *
 * <p>Utilisation : ajouter {@code @ContextConfiguration(initializers = TestcontainersConfig.Initializer.class)}
 * sur la classe de test, ou {@code @Import(TestcontainersConfig.class)}.</p>
 *
 * <p>Requiert Docker sur la machine exécutant les tests.</p>
 */
@Testcontainers
@TestConfiguration
public class TestcontainersConfig {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("kctm_test")
            .withUsername("kct_test")
            .withPassword("kct_test")
            .withReuse(true);

    public static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            postgres.start();

            Map<String, Object> props = Map.of(
                    "spring.datasource.url", postgres.getJdbcUrl() + "?stringtype=unspecified",
                    "spring.datasource.username", postgres.getUsername(),
                    "spring.datasource.password", postgres.getPassword(),
                    "spring.datasource.driver-class-name", postgres.getDriverClassName()
            );

            context.getEnvironment().getPropertySources()
                    .addFirst(new MapPropertySource("testcontainers", props));
        }
    }
}

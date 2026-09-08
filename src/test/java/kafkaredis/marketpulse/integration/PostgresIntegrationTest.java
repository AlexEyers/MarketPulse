package kafkaredis.marketpulse.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

// Idea : Any integration test that extends PostgresIntegrationTest will start Spring Boot using a temporary PostgreSQL DB instead of your localhost:5433 DB
@SpringBootTest
@Transactional // Make every test method transactional (and children's test methods)
// abstract because this base class is not a test by itself, other integration test classes extend it and reuse the container setup
public abstract class PostgresIntegrationTest {

    // One PostgreSQL container shared across all integration test classes.
    // PostgreSQLContainer : A Testcontainers class that starts a real PostgreSQL Docker container
    // "postgres:16-alpine" : The Docker image used for the temporary PostgreSQL database
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    // Start the shared container once when this base class is loaded.
    static {
        postgres.start();
    }

    // Tell Spring to use the shared Testcontainers PostgreSQL DB instead of application.yaml datasource.
    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
// Everything is static because it belongs to the base class, so it is shared by all integration test classes that extend it.

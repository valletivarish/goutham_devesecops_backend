package com.personalfinance.tracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test that verifies the full Spring application context loads
 * without errors when backed by the H2 in-memory database configured
 * in src/test/resources/application.properties.
 *
 * <p>If any bean wiring, configuration, or database-schema problem exists
 * it will be caught here before any feature-level test runs.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationContextTest {

    /**
     * Asserts that the Spring application context starts up successfully.
     * The test body is intentionally empty — a failure to load the context
     * causes JUnit to report a test failure automatically.
     */
    @Test
    void contextLoads() {
        // No assertions needed: the test passes if and only if the
        // application context initialises without throwing an exception.
    }
}

package com.nexarats.academy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class PostgreSqlLiveIntegrationTest {

    @Test
    void validatesLivePostgreSqlSchemaAndContext() {
        // Verifies that Flyway migrations execute successfully on live PostgreSQL
        // and Hibernate schema validation (ddl-auto=validate) passes without error.
    }
}

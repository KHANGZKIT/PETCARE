package com.petcare;

import org.junit.jupiter.api.Test;

/**
 * Basic compilation smoke test.
 * Does NOT attempt to load the Spring context because PostgreSQL
 * may not be available in every developer's environment yet.
 */
class PetCareApplicationTests {

    @Test
    void contextLoadsPlaceholder() {
        // Will be replaced with @SpringBootTest once the database
        // and security configuration are ready for integration testing.
    }

}

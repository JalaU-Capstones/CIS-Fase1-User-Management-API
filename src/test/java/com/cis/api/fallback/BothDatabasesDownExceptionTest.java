package com.cis.api.fallback;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BothDatabasesDownExceptionTest {

    @Test
    void testConstructor() {
        BothDatabasesDownException ex = new BothDatabasesDownException("test message");
        assertThat(ex.getMessage()).isEqualTo("test message");
    }
}

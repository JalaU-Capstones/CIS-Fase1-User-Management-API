package com.cis.api.fallback;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseFallbackPropertiesTest {

    @Test
    void testGettersAndSetters() {
        DatabaseFallbackProperties props = new DatabaseFallbackProperties();
        props.setEnabled(true);
        props.setHealthTtlMs(1000L);
        props.setHealthTimeoutMs(2000L);
        
        assertThat(props.isEnabled()).isTrue();
        assertThat(props.getHealthTtlMs()).isEqualTo(1000L);
        assertThat(props.getHealthTimeoutMs()).isEqualTo(2000L);
    }
}

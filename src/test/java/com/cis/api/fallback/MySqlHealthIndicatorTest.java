package com.cis.api.fallback;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class MySqlHealthIndicatorTest {

    @Test
    void returnsUpWhenSelect1Works() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        given(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).willReturn(1);

        DatabaseFallbackProperties props = new DatabaseFallbackProperties();
        props.setHealthTtlMs(0);

        MySqlHealthIndicator indicator = new MySqlHealthIndicator(jdbcTemplate, props);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
        assertThat(indicator.isUp()).isTrue();
    }

    @Test
    void returnsDownWhenSelect1Throws() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        given(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).willThrow(new RuntimeException("boom"));

        DatabaseFallbackProperties props = new DatabaseFallbackProperties();
        props.setHealthTtlMs(0);

        MySqlHealthIndicator indicator = new MySqlHealthIndicator(jdbcTemplate, props);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
        assertThat(indicator.isUp()).isFalse();
    }
}


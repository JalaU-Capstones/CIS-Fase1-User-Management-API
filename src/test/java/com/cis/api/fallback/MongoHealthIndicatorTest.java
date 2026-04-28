package com.cis.api.fallback;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class MongoHealthIndicatorTest {

    @Test
    void returnsUpWhenPingWorks() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        given(mongoTemplate.executeCommand("{ ping: 1 }")).willReturn(new Document("ok", 1));

        DatabaseFallbackProperties props = new DatabaseFallbackProperties();
        props.setHealthTtlMs(0);

        MongoHealthIndicator indicator = new MongoHealthIndicator(mongoTemplate, props);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
        assertThat(indicator.isUp()).isTrue();
    }

    @Test
    void returnsDownWhenPingThrows() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        given(mongoTemplate.executeCommand("{ ping: 1 }")).willThrow(new RuntimeException("boom"));

        DatabaseFallbackProperties props = new DatabaseFallbackProperties();
        props.setHealthTtlMs(0);

        MongoHealthIndicator indicator = new MongoHealthIndicator(mongoTemplate, props);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
        assertThat(indicator.isUp()).isFalse();
    }
}


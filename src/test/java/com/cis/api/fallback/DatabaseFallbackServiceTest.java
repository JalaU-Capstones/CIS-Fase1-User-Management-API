package com.cis.api.fallback;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class DatabaseFallbackServiceTest {

    @Test
    void whenDisabled_neverSwitchesAndNeverThrows() {
        DatabaseFallbackProperties props = new DatabaseFallbackProperties();
        props.setEnabled(false);

        MySqlHealthIndicator mysql = mock(MySqlHealthIndicator.class);
        MongoHealthIndicator mongo = mock(MongoHealthIndicator.class);
        given(mysql.isUp()).willReturn(false);
        given(mongo.isUp()).willReturn(false);

        DatabaseFallbackService svc = new DatabaseFallbackService(props, mysql, mongo);

        assertThat(svc.isFallbackActive()).isFalse();
        assertThat(svc.getActiveDatabase("v1")).isEqualTo(DatabaseFallbackService.DB_MYSQL);
        assertThat(svc.getActiveDatabase("v2")).isEqualTo(DatabaseFallbackService.DB_MONGO);
    }

    @Test
    void whenBothHealthy_usesDefaultsPerVersion() {
        DatabaseFallbackProperties props = new DatabaseFallbackProperties();
        props.setEnabled(true);

        MySqlHealthIndicator mysql = mock(MySqlHealthIndicator.class);
        MongoHealthIndicator mongo = mock(MongoHealthIndicator.class);
        given(mysql.isUp()).willReturn(true);
        given(mongo.isUp()).willReturn(true);

        DatabaseFallbackService svc = new DatabaseFallbackService(props, mysql, mongo);

        assertThat(svc.isFallbackActive()).isFalse();
        assertThat(svc.getActiveDatabase("v1")).isEqualTo(DatabaseFallbackService.DB_MYSQL);
        assertThat(svc.getActiveDatabase("v2")).isEqualTo(DatabaseFallbackService.DB_MONGO);
    }

    @Test
    void whenMongoDown_routesEverythingToMySql_andFallbackActive() {
        DatabaseFallbackProperties props = new DatabaseFallbackProperties();
        props.setEnabled(true);

        MySqlHealthIndicator mysql = mock(MySqlHealthIndicator.class);
        MongoHealthIndicator mongo = mock(MongoHealthIndicator.class);
        given(mysql.isUp()).willReturn(true);
        given(mongo.isUp()).willReturn(false);

        DatabaseFallbackService svc = new DatabaseFallbackService(props, mysql, mongo);

        assertThat(svc.isFallbackActive()).isTrue();
        assertThat(svc.getActiveDatabase("v1")).isEqualTo(DatabaseFallbackService.DB_MYSQL);
        assertThat(svc.getActiveDatabase("v2")).isEqualTo(DatabaseFallbackService.DB_MYSQL);
    }

    @Test
    void whenMySqlDown_routesEverythingToMongo_andFallbackActive() {
        DatabaseFallbackProperties props = new DatabaseFallbackProperties();
        props.setEnabled(true);

        MySqlHealthIndicator mysql = mock(MySqlHealthIndicator.class);
        MongoHealthIndicator mongo = mock(MongoHealthIndicator.class);
        given(mysql.isUp()).willReturn(false);
        given(mongo.isUp()).willReturn(true);

        DatabaseFallbackService svc = new DatabaseFallbackService(props, mysql, mongo);

        assertThat(svc.isFallbackActive()).isTrue();
        assertThat(svc.getActiveDatabase("v1")).isEqualTo(DatabaseFallbackService.DB_MONGO);
        assertThat(svc.getActiveDatabase("v2")).isEqualTo(DatabaseFallbackService.DB_MONGO);
    }

    @Test
    void whenBothDown_throwsBothDatabasesDownException() {
        DatabaseFallbackProperties props = new DatabaseFallbackProperties();
        props.setEnabled(true);

        MySqlHealthIndicator mysql = mock(MySqlHealthIndicator.class);
        MongoHealthIndicator mongo = mock(MongoHealthIndicator.class);
        given(mysql.isUp()).willReturn(false);
        given(mongo.isUp()).willReturn(false);

        DatabaseFallbackService svc = new DatabaseFallbackService(props, mysql, mongo);

        assertThatThrownBy(() -> svc.getActiveDatabase("v1"))
                .isInstanceOf(BothDatabasesDownException.class);
    }
}


package com.cis.api.fallback;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.junit.jupiter.api.AfterAll;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BothDatabasesDownIntegrationTest {

    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.0")
            .withDatabaseName("sd3")
            .withUsername("sd3user")
            .withPassword("sd3pass");

    static final MongoDBContainer mongo = new MongoDBContainer("mongo:7.0.14");

    static {
        mysql.start();
        mongo.start();
    }

    @AfterAll
    static void cleanupContainers() {
        if (mongo.isRunning()) {
            mongo.stop();
        }
        if (mysql.isRunning()) {
            mysql.stop();
        }
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        // Avoid schema drop on context shutdown after containers are stopped in-test.
        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        r.add("spring.jpa.properties.hibernate.hbm2ddl.auto", () -> "update");
        r.add("spring.jpa.open-in-view", () -> "false");

        r.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);

        r.add("app.fallback.enabled", () -> "true");
        r.add("app.fallback.health-ttl-ms", () -> "0");
        r.add("app.fallback.health-timeout-ms", () -> "500");

        r.add("application-properties.jwt.secret-key", () -> "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        r.add("application-properties.jwt.expiration-time", () -> "864000000");
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void whenBothDatabasesStop_requestsReturnOutageMessage() {
        String baseUrl = "http://localhost:" + port;

        mysql.stop();
        mongo.stop();

        ResponseEntity<String> resp = rest.getForEntity(baseUrl + "/api/v1/users", String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(503);
        assertThat(resp.getBody()).contains("Please try again later");
    }
}

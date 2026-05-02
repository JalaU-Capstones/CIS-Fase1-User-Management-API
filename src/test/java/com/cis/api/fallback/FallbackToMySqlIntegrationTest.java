package com.cis.api.fallback;

import com.cis.api.model.MongoUser;
import com.cis.api.model.User;
import com.cis.api.repository.MongoUserSpringRepository;
import com.cis.api.repository.UserRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.junit.jupiter.api.AfterAll;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FallbackToMySqlIntegrationTest {

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

    @Autowired
    UserRepository userRepository;

    @Autowired
    MongoUserSpringRepository mongoUserRepository;

    @Test
    void whenMongoStops_bothVersionsReadFromMySql_andWritesReturnMaintenanceMessage() {
        // Seed MySQL user
        UUID mysqlId = UUID.randomUUID();
        userRepository.save(new User(mysqlId, "MySQL User", "mysql_user", "pass"));

        // Seed Mongo user
        UUID mongoId = UUID.randomUUID();
        mongoUserRepository.save(new MongoUser(mongoId.toString(), "Mongo User", "mongo_user", "pass"));

        String baseUrl = "http://localhost:" + port;

        // Sanity: each version reads its default at first
        ResponseEntity<String> v1Before = rest.getForEntity(baseUrl + "/api/v1/users", String.class);
        ResponseEntity<String> v2Before = rest.getForEntity(baseUrl + "/api/v2/users", String.class);
        assertThat(v1Before.getStatusCode().value()).isEqualTo(200);
        assertThat(v1Before.getBody()).contains("mysql_user");
        assertThat(v2Before.getStatusCode().value()).isEqualTo(200);
        assertThat(v2Before.getBody()).contains("mongo_user");

        // Simulate Mongo outage
        mongo.stop();

        // Now both versions should read from MySQL (fallback)
        ResponseEntity<String> v1After = rest.getForEntity(baseUrl + "/api/v1/users", String.class);
        ResponseEntity<String> v2After = rest.getForEntity(baseUrl + "/api/v2/users", String.class);
        assertThat(v1After.getStatusCode().value()).isEqualTo(200);
        assertThat(v2After.getStatusCode().value()).isEqualTo(200);
        assertThat(v1After.getBody()).contains("mysql_user");
        assertThat(v2After.getBody()).contains("mysql_user").doesNotContain("mongo_user");

        // Any write should return 503 maintenance message. Use auth endpoint (public POST).
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{\"login\":\"x\",\"password\":\"y\"}", headers);
        ResponseEntity<String> loginAttempt = rest.exchange(
                baseUrl + "/api/v2/auth/login",
                HttpMethod.POST,
                entity,
                String.class
        );
        assertThat(loginAttempt.getStatusCode().value()).isEqualTo(503);
        assertThat(loginAttempt.getBody()).contains("planned maintenance");
    }
}

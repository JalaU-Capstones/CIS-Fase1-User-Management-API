package com.cis.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@TestPropertySource(properties = {
        "application-properties.jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "application-properties.jwt.expiration-time=864000000"
})
class CisUserManagementApiPhase1ApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void main() {
        // Use an ephemeral port so the test does not fail when 8080 is already bound
        // (e.g. developer running the API locally while executing tests).
        assertThatCode(() -> CisUserManagementApiPhase1Application.main(new String[]{"--server.port=0"}))
                .doesNotThrowAnyException();
    }

}

package com.cis.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "application-properties.jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "application-properties.jwt.expiration-time=864000000"
})
class CisUserManagementApiPhase1ApplicationTests {

    @Test
    void contextLoads() {
    }

}

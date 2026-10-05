package com.coworking.reservations;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                "app.security.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
        }
)
@ActiveProfiles("dev")
class CoworkingReservationsApplicationTests {

    @Test
    void contextLoads() {
    }
}
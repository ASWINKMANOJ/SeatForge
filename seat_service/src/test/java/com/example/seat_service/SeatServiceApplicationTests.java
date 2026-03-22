package com.example.seat_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/testdb",
        "SPRING_DATASOURCE_USERNAME=test",
        "SPRING_DATASOURCE_PASSWORD=test",
        "SPRING_REDIS_HOST=localhost",
        "AUTH0_DOMAIN=test.auth0.com",
        "AUTH0_AUDIENCE=test-audience"
})
class SeatServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}

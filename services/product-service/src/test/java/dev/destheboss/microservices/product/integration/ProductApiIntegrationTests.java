package dev.destheboss.microservices.product.integration;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true) // This also is not necessary, but kept in case we are not running the container.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // Only needed if the application.properties is accurate (in this service's case it is, but we are keeping it as a reminder for other services if we forgot to set it up).
class ProductApiIntegrationTests {

    @ServiceConnection
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine"); // This can be removed if we are running the docker container for the product service because we already have the TestcontainersConfiguration class which contains the Bean.

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void shouldCreateProduct() {
        String requestBody = """
                {
                    "skuCode": "SANDWICH001",
                    "name": "Sandwich",
                    "description": "Has ham and lettuce.",
                    "price": 5
                }
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/product")
                .then()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("skuCode", Matchers.equalTo("SANDWICH001"))
                .body("name", Matchers.equalTo("Sandwich"))
                .body("description", Matchers.equalTo("Has ham and lettuce."))
                .body("price", Matchers.equalTo(5));
    }
}

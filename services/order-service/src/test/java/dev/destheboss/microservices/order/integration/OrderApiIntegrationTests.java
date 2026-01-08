package dev.destheboss.microservices.order.integration;

import dev.destheboss.microservices.order.event.InventoryReserveFailedEvent;
import dev.destheboss.microservices.order.event.InventoryReservedEvent;
import dev.destheboss.microservices.order.persistence.OrderRepository;
import io.restassured.RestAssured;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {
        "inventory-reserve-requested",
        "inventory-reserved",
        "inventory-reserve-failed",
        "order-confirmed",
        "order-cancelled"
})
class OrderApiIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @LocalServerPort
    private Integer port;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void shouldSubmitOrderAndRemainPendingInitially() {
        String submitOrderJson = """
                {
                    "skuCode": "iphone_15",
                    "price": 1,
                    "quantity": 1,
                    "userDetails": {
                        "email": "test@example.com"
                    }
                }
                """;

        String responseBodyString = RestAssured.given()
                .contentType("application/json")
                .body(submitOrderJson)
                .when()
                .post("/api/order")
                .then()
                .statusCode(201)
                .extract()
                .asString();

        assertThat(responseBodyString, Matchers.is("Order received (PENDING)"));
    }

    @Test
    void shouldConfirmOrderWhenInventoryReservedEventArrives() {
        // 1) Place order
        String submitOrderJson = """
                {
                    "skuCode": "iphone_15",
                    "price": 1,
                    "quantity": 1,
                    "userDetails": { "email": "test@example.com" }
                }
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(submitOrderJson)
                .when()
                .post("/api/order")
                .then()
                .statusCode(201);

        // 2) Get the created orderNumber from DB (latest)
        var order = orderRepository.findAll()
                .stream()
                .reduce((first, second) -> second)
                .orElseThrow();

        // 3) Simulate inventory success (Kafka)
        kafkaTemplate.send("inventory-reserved", new InventoryReservedEvent(order.getOrderNumber(), "test@example.com"));

        // 4) Assert status eventually becomes CONFIRMED
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var refreshed = orderRepository.findByOrderNumber(order.getOrderNumber()).orElseThrow();
                    org.junit.jupiter.api.Assertions.assertEquals(
                            dev.destheboss.microservices.order.model.OrderStatus.CONFIRMED,
                            refreshed.getStatus()
                    );
                });
    }

    @Test
    void shouldCancelOrderWhenInventoryReserveFailedEventArrives() {
        // 1) Place order
        String submitOrderJson = """
                {
                    "skuCode": "iphone_15",
                    "price": 1,
                    "quantity": 9999,
                    "userDetails": { "email": "test@example.com" }
                }
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(submitOrderJson)
                .when()
                .post("/api/order")
                .then()
                .statusCode(201);

        // 2) Find created orderNumber
        var order = orderRepository.findAll()
                .stream()
                .reduce((first, second) -> second)
                .orElseThrow();

        // 3) Simulate inventory failure (Kafka)
        kafkaTemplate.send("inventory-reserve-failed",
                new InventoryReserveFailedEvent(order.getOrderNumber(), "test@example.com", "Insufficient stock"));

        // 4) Assert status becomes CANCELLED
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var refreshed = orderRepository.findByOrderNumber(order.getOrderNumber()).orElseThrow();
                    org.junit.jupiter.api.Assertions.assertEquals(
                            dev.destheboss.microservices.order.model.OrderStatus.CANCELLED,
                            refreshed.getStatus()
                    );
                });
    }
}

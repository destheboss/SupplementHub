package dev.destheboss.microservices.inventory.business;

import dev.destheboss.microservices.order.event.InventoryReserveFailedEvent;
import dev.destheboss.microservices.order.event.InventoryReserveRequestedEvent;
import dev.destheboss.microservices.order.event.InventoryReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryReservationListener {
    private final InventoryService inventoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "inventory-reserve-requested")
    public void onReserveRequested(InventoryReserveRequestedEvent event) {
        log.info("Received InventoryReserveRequestedEvent: {}", event);

        boolean reserved = inventoryService.reserve(event.getSkuCode(), event.getQuantity());

        if (reserved) {
            InventoryReservedEvent success = new InventoryReservedEvent(event.getOrderNumber(), event.getEmail());
            log.info("Publishing InventoryReservedEvent: {}", success);
            kafkaTemplate.send("inventory-reserved", success);
        } else {
            InventoryReserveFailedEvent failed = new InventoryReserveFailedEvent(
                    event.getOrderNumber(),
                    event.getEmail(),
                    "Insufficient stock for sku=" + event.getSkuCode()
            );
            log.info("Publishing InventoryReserveFailedEvent: {}", failed);
            kafkaTemplate.send("inventory-reserve-failed", failed);
        }
    }
}

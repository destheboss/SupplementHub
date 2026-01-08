package dev.destheboss.microservices.order.business;

import dev.destheboss.microservices.order.event.InventoryReserveFailedEvent;
import dev.destheboss.microservices.order.event.InventoryReservedEvent;
import dev.destheboss.microservices.order.event.OrderCancelledEvent;
import dev.destheboss.microservices.order.event.OrderConfirmedEvent;
import dev.destheboss.microservices.order.model.OrderStatus;
import dev.destheboss.microservices.order.persistence.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSagaListener {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "inventory-reserved")
    @Transactional
    public void onInventoryReserved(InventoryReservedEvent event) {
        log.info("Inventory reserved for order {}", event.getOrderNumber());

        var order = orderRepository.findByOrderNumber(event.getOrderNumber())
                .orElseThrow(() -> new IllegalStateException("Order not found: " + event.getOrderNumber()));

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        OrderConfirmedEvent confirmed = new OrderConfirmedEvent(order.getOrderNumber(), event.getEmail());
        kafkaTemplate.send("order-confirmed", confirmed);
        log.info("Published OrderConfirmedEvent: {}", confirmed);
    }

    @KafkaListener(topics = "inventory-reserve-failed")
    @Transactional
    public void onInventoryReserveFailed(InventoryReserveFailedEvent event) {
        log.info("Inventory reservation failed for order {} reason={}", event.getOrderNumber(), event.getReason());

        var order = orderRepository.findByOrderNumber(event.getOrderNumber())
                .orElseThrow(() -> new IllegalStateException("Order not found: " + event.getOrderNumber()));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        OrderCancelledEvent cancelled = new OrderCancelledEvent(order.getOrderNumber(), event.getEmail(), event.getReason());
        kafkaTemplate.send("order-cancelled", cancelled);
        log.info("Published OrderCancelledEvent: {}", cancelled);
    }
}

package dev.destheboss.microservices.order.business;

import dev.destheboss.microservices.order.dto.OrderRequest;
import dev.destheboss.microservices.order.event.InventoryReserveRequestedEvent;
import dev.destheboss.microservices.order.model.Order;
import dev.destheboss.microservices.order.model.OrderStatus;
import dev.destheboss.microservices.order.persistence.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, InventoryReserveRequestedEvent> kafkaTemplate;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderService.class);

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setPrice(orderRequest.price());
        order.setSkuCode(orderRequest.skuCode());
        order.setQuantity(orderRequest.quantity());
        order.setStatus(OrderStatus.PENDING);

        orderRepository.save(order);

        InventoryReserveRequestedEvent event = new InventoryReserveRequestedEvent(
                order.getOrderNumber(),
                order.getSkuCode(),
                order.getQuantity(),
                orderRequest.userDetails().email()
        );

        log.info("Sending InventoryReserveRequestedEvent {} to topic 'inventory-reserve-requested'", event);
        kafkaTemplate.send("inventory-reserve-requested", event);
    }
}

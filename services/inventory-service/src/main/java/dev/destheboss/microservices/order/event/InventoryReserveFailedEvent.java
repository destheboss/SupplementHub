package dev.destheboss.microservices.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReserveFailedEvent {
    private String orderNumber;
    private String email;
    private String reason;
}

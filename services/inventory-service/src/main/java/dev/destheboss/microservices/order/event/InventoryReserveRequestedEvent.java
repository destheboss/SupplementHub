package dev.destheboss.microservices.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReserveRequestedEvent {
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
    private String email;
}

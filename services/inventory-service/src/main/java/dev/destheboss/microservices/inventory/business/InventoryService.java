package dev.destheboss.microservices.inventory.business;

import dev.destheboss.microservices.inventory.persistence.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public boolean isInStock(String skuCode, Integer quantity) {
        return inventoryRepository.existsBySkuCodeAndQuantityGreaterThanEqual(skuCode, quantity);
    }

    @Transactional
    public boolean reserve(String skuCode, Integer quantity) {
        return inventoryRepository.reserveIfAvailable(skuCode, quantity) == 1;
    }
}

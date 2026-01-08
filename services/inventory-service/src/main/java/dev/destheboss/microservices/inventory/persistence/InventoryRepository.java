package dev.destheboss.microservices.inventory.persistence;

import dev.destheboss.microservices.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    boolean existsBySkuCodeAndQuantityGreaterThanEqual(String skuCode, Integer quantity);

    @Modifying
    @Query("""
        UPDATE Inventory i
        SET i.quantity = i.quantity - :quantity
        WHERE i.skuCode = :skuCode AND i.quantity >= :quantity
    """)
    int reserveIfAvailable(@Param("skuCode") String skuCode, @Param("quantity") Integer quantity);
}

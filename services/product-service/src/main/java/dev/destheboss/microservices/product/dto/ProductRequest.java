package dev.destheboss.microservices.product.dto;

import java.math.BigDecimal;

public record ProductRequest(Long id, String skuCode, String name, String description, BigDecimal price) {
}

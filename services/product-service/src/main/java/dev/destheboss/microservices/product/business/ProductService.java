package dev.destheboss.microservices.product.business;

import dev.destheboss.microservices.product.dto.ProductRequest;
import dev.destheboss.microservices.product.dto.ProductResponse;
import dev.destheboss.microservices.product.model.Product;
import dev.destheboss.microservices.product.persistence.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .skuCode(productRequest.skuCode())
                .name(productRequest.name())
                .description(productRequest.description())
                .price(productRequest.price())
                .build();
        productRepository.save(product);

        String message = "Product created successfully";
        log.info(message);
        return new ProductResponse(product.getId(), product.getSkuCode(), product.getName(), product.getDescription(), product.getPrice());
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(product -> new ProductResponse(product.getId(), product.getSkuCode(), product.getName(), product.getDescription(), product.getPrice()))
                .toList();
    }
}

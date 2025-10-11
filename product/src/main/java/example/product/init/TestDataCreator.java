package example.product.init;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import example.product.domain.Product;
import example.product.infrastructure.ProductRepository;

@Component
public class TestDataCreator {

    private final ProductRepository productRepository;

    public TestDataCreator(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PostConstruct
    public void createTestData() {
        Product product1 = new Product(100L, 100L);
        Product product2 = new Product(100L, 200L);

        productRepository.save(product1);
        productRepository.save(product2);
    }
}

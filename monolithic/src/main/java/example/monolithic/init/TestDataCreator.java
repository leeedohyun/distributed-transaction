package example.monolithic.init;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import example.monolithic.point.domain.Point;
import example.monolithic.point.infrastructure.PointRepository;
import example.monolithic.product.domain.Product;
import example.monolithic.product.infrastructure.ProductRepository;

@Component
public class TestDataCreator {

    private final PointRepository pointRepository;
    private final ProductRepository productRepository;

    public TestDataCreator(PointRepository pointRepository, ProductRepository productRepository) {
        this.pointRepository = pointRepository;
        this.productRepository = productRepository;
    }

    @PostConstruct
    public void createTestData() {
        pointRepository.save(new Point(1L, 10000L));

        Product product1 = new Product(100L, 100L);
        Product product2 = new Product(100L, 200L);

        productRepository.save(product1);
        productRepository.save(product2);
    }
}

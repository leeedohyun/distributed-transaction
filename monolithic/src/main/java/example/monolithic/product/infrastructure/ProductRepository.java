package example.monolithic.product.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import example.monolithic.product.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}

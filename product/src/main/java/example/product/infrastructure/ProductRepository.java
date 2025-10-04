package example.product.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import example.product.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}

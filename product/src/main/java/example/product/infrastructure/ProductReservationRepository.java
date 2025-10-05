package example.product.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import example.product.domain.ProductReservation;

public interface ProductReservationRepository extends JpaRepository<ProductReservation, Long> {

    List<ProductReservation> findAllByRequestId(String requestId);
}

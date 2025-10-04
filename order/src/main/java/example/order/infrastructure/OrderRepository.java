package example.order.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import example.order.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}

package example.monolithic.order.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import example.monolithic.order.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}

package example.monolithic.order.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import example.monolithic.order.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}

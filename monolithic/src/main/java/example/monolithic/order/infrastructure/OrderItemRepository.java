package example.monolithic.order.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import example.monolithic.order.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrderId(Long orderId);
}

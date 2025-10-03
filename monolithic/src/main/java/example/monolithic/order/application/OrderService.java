package example.monolithic.order.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import example.monolithic.order.application.dto.PlaceOrderCommand;
import example.monolithic.order.domain.Order;
import example.monolithic.order.domain.OrderItem;
import example.monolithic.order.infrastructure.OrderItemRepository;
import example.monolithic.order.infrastructure.OrderRepository;
import example.monolithic.point.application.PointService;
import example.monolithic.product.application.ProductService;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PointService pointService;
    private final ProductService productService;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            PointService pointService,
            ProductService productService
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.pointService = pointService;
        this.productService = productService;
    }

    @Transactional
    public void placeOrder(PlaceOrderCommand command) {
        Order order = orderRepository.save(new Order());
        Long totalPrice = 0L;

        for (PlaceOrderCommand.OrderItem item : command.orderItems()) {
            OrderItem orderItem = new OrderItem(order.getId(), item.productId(), item.quantity());
            orderItemRepository.save(orderItem);

            Long price = productService.buy(item.productId(), item.quantity());
            totalPrice += price;
        }

        pointService.use(1L, totalPrice);
    }
}

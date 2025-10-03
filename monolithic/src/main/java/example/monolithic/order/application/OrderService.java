package example.monolithic.order.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import example.monolithic.order.application.dto.CreateOrderCommand;
import example.monolithic.order.application.dto.CreateOrderResult;
import example.monolithic.order.application.dto.PlaceOrderCommand;
import example.monolithic.order.domain.Order;
import example.monolithic.order.domain.Order.OrderStatus;
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
    public CreateOrderResult createOrder(CreateOrderCommand command) {
        Order order = orderRepository.save(new Order());
        List<OrderItem> orderItems = command.orderItems()
                .stream()
                .map(item -> new OrderItem(order.getId(), item.productId(), item.quantity()))
                .toList();

        orderItemRepository.saveAll(orderItems);

        return new CreateOrderResult(order.getId());
    }

    @Transactional
    public void placeOrder(PlaceOrderCommand command) throws InterruptedException {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new RuntimeException("주문 정보가 존재하지 않습니다."));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            return;
        }

        Long totalPrice = 0L;
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(order.getId());

        for (OrderItem item : orderItems) {
            OrderItem orderItem = new OrderItem(order.getId(), item.getProductId(), item.getQuantity());
            orderItemRepository.save(orderItem);

            Long price = productService.buy(item.getProductId(), item.getQuantity());
            totalPrice += price;
        }

        pointService.use(1L, totalPrice);

        order.complete();
        orderRepository.save(order);

        System.out.println("결제 완료!!");
        Thread.sleep(3000);
    }
}

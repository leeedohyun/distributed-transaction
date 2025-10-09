package example.order.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import example.order.application.dto.CreateOrderCommand;
import example.order.application.dto.CreateOrderResult;
import example.order.application.dto.OrderDto;
import example.order.application.dto.PlaceOrderCommand;
import example.order.domain.Order;
import example.order.domain.Order.OrderStatus;
import example.order.domain.OrderItem;
import example.order.infrastructure.OrderItemRepository;
import example.order.infrastructure.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public OrderDto getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(order.getId());

        return new OrderDto(
                orderItems.stream().map(item -> new OrderDto.OrderItem(item.getProductId(), item.getQuantity())).toList()
        );
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

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(order.getId());

        for (OrderItem item : orderItems) {
            OrderItem orderItem = new OrderItem(order.getId(), item.getProductId(), item.getQuantity());
            orderItemRepository.save(orderItem);

        }

        order.complete();
        orderRepository.save(order);

        System.out.println("결제 완료!!");
        Thread.sleep(3000);
    }

    @Transactional
    public void reserve(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        order.reserve();
        orderRepository.save(order);
    }

    @Transactional
    public void cancel(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        order.cancel();
        orderRepository.save(order);
    }

    @Transactional
    public void confirm(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        order.confirm();
        orderRepository.save(order);
    }

    @Transactional
    public void pending(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        order.pending();
        orderRepository.save(order);
    }
}

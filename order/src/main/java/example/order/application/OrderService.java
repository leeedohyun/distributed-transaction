package example.order.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import example.order.application.dto.CreateOrderCommand;
import example.order.application.dto.CreateOrderResult;
import example.order.application.dto.OrderDto;
import example.order.application.dto.PlaceOrderCommand;
import example.order.domain.Order;
import example.order.domain.OrderItem;
import example.order.infrastructure.OrderItemRepository;
import example.order.infrastructure.OrderRepository;
import example.order.infrastructure.kafka.OrderPlacedProducer;
import example.order.infrastructure.kafka.dto.OrderPlacedEvent;

@Service
public class OrderService {

    public final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderPlacedProducer orderPlacedProducer;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, OrderPlacedProducer orderPlacedProducer) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderPlacedProducer = orderPlacedProducer;
    }

    @Transactional
    public CreateOrderResult createOrder(CreateOrderCommand command) {
        Order order = orderRepository.save(new Order());

        List<OrderItem> orderItems = command.items()
                .stream()
                .map(item -> new OrderItem(order.getId(), item.productId(), item.quantity()))
                .toList();

        orderItemRepository.saveAll(orderItems);

        return new CreateOrderResult(order.getId());
    }

    public OrderDto getOrder(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(orderId);

        return new OrderDto(
                orderItems.stream()
                        .map(item -> new OrderDto.OrderItem(item.getProductId(), item.getQuantity()))
                        .toList()
        );
    }

    @Transactional
    public  void placeOrder(PlaceOrderCommand command) {
        Order order = orderRepository.findById(command.orderId()).orElseThrow();
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(order.getId());

        order.request();
        orderRepository.save(order);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderPlacedProducer.send(
                        new OrderPlacedEvent(
                                command.orderId(),
                                orderItems.stream()
                                        .map(orderItem -> new OrderPlacedEvent.ProductInfo(orderItem.getProductId(), orderItem.getQuantity()))
                                        .toList()
                        )
                );
            }
        });
    }

    @Transactional
    public void request(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        order.request();
        orderRepository.save(order);
    }

    @Transactional
    public void complete(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        order.complete();
        orderRepository.save(order);
    }

    @Transactional
    public void fail(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        order.fail();
        orderRepository.save(order);
    }
}

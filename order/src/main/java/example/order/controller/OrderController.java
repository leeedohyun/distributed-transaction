package example.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import example.order.application.OrderCoordinator;
import example.order.application.OrderService;
import example.order.application.RedisLockService;
import example.order.application.dto.CreateOrderResult;
import example.order.controller.dto.CreateOrderRequest;
import example.order.controller.dto.CreateOrderResponse;
import example.order.controller.dto.PlaceOrderRequest;
import example.order.domain.Order;

@RestController
public class OrderController {

    private final OrderService orderService;
    private final OrderCoordinator orderCoordinator;
    private final RedisLockService redisLockService;

    public OrderController(OrderService orderService, RedisLockService redisLockService, OrderCoordinator orderCoordinator) {
        this.orderService = orderService;
        this.orderCoordinator = orderCoordinator;
        this.redisLockService = redisLockService;
    }

    @PostMapping("/order")
    public CreateOrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        CreateOrderResult result = orderService.createOrder(request.toCommand());

        return new CreateOrderResponse(result.orderId());
    }

    @PostMapping("/order/place")
    public void placeOrder(@RequestBody PlaceOrderRequest request) {
        String lockKey = "order:" + request.orderId();

        boolean lockAcquired = redisLockService.tryLock(lockKey, request.orderId().toString());

        if (!lockAcquired) {
            throw new RuntimeException("락 획득에 실패하였습니다.");
        }

        try {
            orderService.placeOrder(request.toCommand());
        } finally {
            redisLockService.releaseLock(lockKey);
        }
    }

    @GetMapping("/order/{id}/status")
    public Order.OrderStatus getStatus(@PathVariable("id") Long id) {
        return orderService.getStatus(id);
    }
}

package example.monolithic.order.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import example.monolithic.order.application.OrderService;
import example.monolithic.order.controller.dto.PlaceOrderRequest;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/order/place")
    public void placeOrder(
            @RequestBody PlaceOrderRequest request
    ) {
        orderService.placeOrder(request.toPlaceOrderCommand());
    }
}

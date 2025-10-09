package example.order.controller.dto;

import java.util.List;

import example.order.application.dto.CreateOrderCommand;

public record CreateOrderRequest(List<OrderItem> orderItems) {

    public CreateOrderCommand toCreateOrderCommand() {
        return new CreateOrderCommand(
                orderItems.stream()
                        .map(item -> new CreateOrderCommand.OrderItem(item.productId, item.quantity))
                        .toList()
        );
    }

    public record OrderItem(Long productId, Long quantity) {}
}

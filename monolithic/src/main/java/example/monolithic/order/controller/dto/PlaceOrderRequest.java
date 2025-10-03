package example.monolithic.order.controller.dto;

import java.util.List;

import example.monolithic.order.application.dto.PlaceOrderCommand;

public record PlaceOrderRequest(List<OrderItem> orderItems) {

    public PlaceOrderCommand toPlaceOrderCommand() {
        return new PlaceOrderCommand(
                orderItems.stream()
                        .map(item -> new PlaceOrderCommand.OrderItem(
                                item.productId,
                                item.quantity
                        ))
                        .toList()
        );
    }

    public record OrderItem(Long productId, Long quantity) {}
}

package example.order.controller.dto;

import example.order.application.dto.PlaceOrderCommand;

public record PlaceOrderRequest(Long orderId) {

    public PlaceOrderCommand toCommand() {
        return new PlaceOrderCommand(orderId);
    }
}

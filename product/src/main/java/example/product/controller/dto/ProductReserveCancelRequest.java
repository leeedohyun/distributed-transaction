package example.product.controller.dto;

import example.product.application.dto.ProductReserveCancelCommand;

public record ProductReserveCancelRequest(String requestId) {

    public ProductReserveCancelCommand toCommand() {
        return new ProductReserveCancelCommand(requestId);
    }
}

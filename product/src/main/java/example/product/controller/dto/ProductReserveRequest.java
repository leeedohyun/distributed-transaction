package example.product.controller.dto;

import java.util.List;

import example.product.application.dto.ProductReserveCommand;

public record ProductReserveRequest(String requestId, List<ReserveItem> items) {

    public ProductReserveCommand toCommand() {
        return new ProductReserveCommand(
                requestId,
                items.stream()
                        .map(item -> new ProductReserveCommand.ReserveItem(item.productId, item.reserveQuantity))
                        .toList()
        );
    }

    public record ReserveItem(Long productId, Long reserveQuantity) {
    }
}

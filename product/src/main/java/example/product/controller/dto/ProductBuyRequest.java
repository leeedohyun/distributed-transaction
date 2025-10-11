package example.product.controller.dto;

import java.util.List;

import example.product.application.dto.ProductBuyCommand;

public record ProductBuyRequest(String requestId, List<ProductInfo> productInfos) {

    public ProductBuyCommand toCommand() {
        return new ProductBuyCommand(
            requestId,
            productInfos.stream()
                .map(info -> new ProductBuyCommand.ProductInfo(info.productId(), info.quantity()))
                .toList()
        );
    }

    public record ProductInfo(Long productId, Long quantity) {
    }
}

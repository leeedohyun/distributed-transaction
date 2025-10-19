package example.product.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import example.product.application.ProductService;
import example.product.application.dto.ProductBuyCancelCommand;
import example.product.application.dto.ProductBuyCommand;
import example.product.application.dto.ProductBuyCommand.ProductInfo;
import example.product.application.dto.ProductBuyResult;
import example.product.consumer.dto.OrderPlacedEvent;
import example.product.infrastructure.kafka.QuantityDecreasedFailProducer;
import example.product.infrastructure.kafka.QuantityDecreasedProducer;
import example.product.infrastructure.kafka.dto.QuantityDecreasedEvent;
import example.product.infrastructure.kafka.dto.QuantityDecreasedFailEvent;

@Component
public class OrderPlacedConsumer {

    private final ProductService productService;
    private final QuantityDecreasedProducer quantityDecreasedProducer;
    private final QuantityDecreasedFailProducer quantityDecreasedFailProducer;

    public OrderPlacedConsumer(ProductService productService, QuantityDecreasedProducer quantityDecreasedProducer, QuantityDecreasedFailProducer quantityDecreasedFailProducer) {
        this.productService = productService;
        this.quantityDecreasedProducer = quantityDecreasedProducer;
        this.quantityDecreasedFailProducer = quantityDecreasedFailProducer;
    }

    @KafkaListener(
            topics = "order-placed",
            groupId = "order-placed-consumer",
            properties = {
                    "spring.json.value.default.type=example.product.consumer.dto.OrderPlacedEvent"
            }
    )
    public void handle(OrderPlacedEvent event) {
        String requestId = event.orderId().toString();

        try {
            ProductBuyResult result = productService.buy(
                    new ProductBuyCommand(
                            requestId,
                            event.productInfos()
                                    .stream()
                                    .map(item -> new ProductInfo(item.productId(), item.quantity()))
                                    .toList()
                    )
            );

            quantityDecreasedProducer.send(
                    new QuantityDecreasedEvent(
                            event.orderId(),
                            result.totalPrice()
                    )
            );
        } catch (Exception e) {
            productService.cancel(new ProductBuyCancelCommand(requestId));

            quantityDecreasedFailProducer.send(new QuantityDecreasedFailEvent(event.orderId()));
        }
    }
}

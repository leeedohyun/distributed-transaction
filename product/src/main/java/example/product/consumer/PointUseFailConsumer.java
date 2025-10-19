package example.product.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import example.product.application.ProductService;
import example.product.application.dto.ProductBuyCancelCommand;
import example.product.consumer.dto.PointUseFailEvent;
import example.product.infrastructure.kafka.QuantityDecreasedFailProducer;
import example.product.infrastructure.kafka.dto.QuantityDecreasedFailEvent;

@Component
public class PointUseFailConsumer {

    private final ProductService productService;
    private final QuantityDecreasedFailProducer quantityDecreasedFailProducer;

    public PointUseFailConsumer(ProductService productService, QuantityDecreasedFailProducer quantityDecreasedFailProducer) {
        this.productService = productService;
        this.quantityDecreasedFailProducer = quantityDecreasedFailProducer;
    }

    @KafkaListener(
            topics = "point-use-fail",
            groupId = "point-use-fail-consumer",
            properties = {
                    "spring.json.value.default.type=example.product.consumer.dto.PointUseFailEvent"
            }
    )
    public void handle(PointUseFailEvent event) {
        productService.cancel(new ProductBuyCancelCommand(event.orderId().toString()));

        quantityDecreasedFailProducer.send(new QuantityDecreasedFailEvent(event.orderId()));
    }
}

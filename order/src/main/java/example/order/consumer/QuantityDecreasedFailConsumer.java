package example.order.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import example.order.application.OrderService;
import example.order.consumer.dto.QuantityDecreasedFailEvent;

@Component
public class QuantityDecreasedFailConsumer {

    private final OrderService orderService;

    public QuantityDecreasedFailConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(
            topics = "quantity-decreased-fail",
            groupId = "quantity-decrease-fail-consumer",
            properties = {
                    "spring.json.value.default.type=example.order.consumer.dto.QuantityDecreasedFailEvent"
            }
    )
    public void handle(QuantityDecreasedFailEvent event) {
        orderService.fail(event.orderId());
    }
}

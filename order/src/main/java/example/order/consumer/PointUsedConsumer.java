package example.order.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import example.order.application.OrderService;
import example.order.consumer.dto.PointUsedEvent;

@Component
public class PointUsedConsumer {

    private final OrderService orderService;

    public PointUsedConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(
            topics = "point-used",
            groupId = "point-used-consumer",
            properties = {
                    "spring.json.value.default.type=example.order.consumer.dto.PointUsedEvent"
            }
    )
    public void handle(PointUsedEvent event) {
        orderService.complete(event.orderId());
    }
}

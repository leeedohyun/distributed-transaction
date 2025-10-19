package example.point.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import example.point.application.PointService;
import example.point.application.dto.PointUseCancelCommand;
import example.point.application.dto.PointUseCommand;
import example.point.consumer.dto.QuantityDecreasedEvent;
import example.point.infrastructure.kafka.PointUseFailProducer;
import example.point.infrastructure.kafka.PointUsedProducer;
import example.point.infrastructure.kafka.dto.PointUseFailEvent;
import example.point.infrastructure.kafka.dto.PointUsedEvent;

@Component
public class QuantityDecreasedConsumer {

    private final PointService pointService;
    private final PointUsedProducer pointUsedProducer;
    private final PointUseFailProducer pointUseFailProducer;

    public QuantityDecreasedConsumer(PointService pointService, PointUsedProducer pointUsedProducer, PointUseFailProducer pointUseFailProducer) {
        this.pointService = pointService;
        this.pointUsedProducer = pointUsedProducer;
        this.pointUseFailProducer = pointUseFailProducer;
    }

    @KafkaListener(
            topics = "quantity-decreased",
            groupId = "quantity-decrease-consumer",
            properties = {
                    "spring.json.value.default.type=example.point.consumer.dto.QuantityDecreasedEvent"
            }
    )
    public void handle(QuantityDecreasedEvent event) {
        String requestId = event.orderId().toString();

        try {
            pointService.use(new PointUseCommand(requestId, 1L, event.totalPrice()));

            pointUsedProducer.send(new PointUsedEvent(event.orderId()));
        } catch (Exception e) {
            pointService.cancel(new PointUseCancelCommand(requestId));

            pointUseFailProducer.send(new PointUseFailEvent(event.orderId()));
        }
    }
}

package example.point.infrastructure.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import example.point.infrastructure.kafka.dto.PointUseFailEvent;

@Component
public class PointUseFailProducer {

    private final KafkaTemplate<String, PointUseFailEvent> kafkaTemplate;

    public PointUseFailProducer(KafkaTemplate<String, PointUseFailEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(PointUseFailEvent event) {
        kafkaTemplate.send("point-use-fail", event.orderId().toString(), event);
    }
}

package example.product.infrastructure.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import example.product.infrastructure.kafka.dto.QuantityDecreasedFailEvent;

@Component
public class QuantityDecreasedFailProducer {

    private final KafkaTemplate<String, QuantityDecreasedFailEvent> kafkaTemplate;

    public QuantityDecreasedFailProducer(KafkaTemplate<String, QuantityDecreasedFailEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(QuantityDecreasedFailEvent event) {
        kafkaTemplate.send("quantity-decreased-fail", event);
    }
}

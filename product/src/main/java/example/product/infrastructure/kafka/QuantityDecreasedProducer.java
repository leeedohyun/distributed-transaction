package example.product.infrastructure.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import example.product.infrastructure.kafka.dto.QuantityDecreasedEvent;

@Component
public class QuantityDecreasedProducer {

    private final KafkaTemplate<String, QuantityDecreasedEvent> kafkaTemplate;

    public QuantityDecreasedProducer(KafkaTemplate<String, QuantityDecreasedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(QuantityDecreasedEvent event) {
        kafkaTemplate.send("quantity-decreased", event);
    }
}

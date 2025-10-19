package example.order.infrastructure.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import example.order.infrastructure.kafka.dto.OrderPlacedEvent;

@Component
public class OrderPlacedProducer {

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public OrderPlacedProducer(KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 이벤트를 발행할 때 동일한 주문에 대한 이벤트는 한 번만 발행이 되어야 함
     * -> 키를 지정해서 동일한 파티션으로 이벤트를 발송할 수 있도록 해야 함
     */
    public void send(OrderPlacedEvent event) {
        kafkaTemplate.send("order-placed", event.orderId().toString(), event);
    }
}

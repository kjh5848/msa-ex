package com.metacoding.order.adapter.producer;

import com.metacoding.order.adapter.message.OrderCreated;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishOrderCreated(OrderCreated event) {
        kafkaTemplate.send("order-created", event);
        System.out.println("order 이벤트 생성");
    }
}



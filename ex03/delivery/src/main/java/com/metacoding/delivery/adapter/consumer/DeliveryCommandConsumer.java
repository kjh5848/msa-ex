package com.metacoding.delivery.adapter.consumer;

import com.metacoding.delivery.adapter.message.*;
import com.metacoding.delivery.usecase.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeliveryCommandConsumer {
    private final DeliveryService deliveryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Transactional
    @KafkaListener(topics = "create-delivery-command", groupId = "delivery-service")
    public void handleCreateDeliveryCommand(CreateDeliveryCommand command) {
        try {
            // 배달 생성 처리
            var result = deliveryService.saveDelivery(command.getOrderId(), command.getAddress());
            
            // 성공 이벤트 발행
            DeliveryCreated event = new DeliveryCreated(
                command.getOrderId(),
                result.id(),
                true
            );
            kafkaTemplate.send("delivery-created", event);
            System.out.println("delivery 이벤트 생성 성공");
        } catch (Exception e) {
            // 실패 이벤트 발행
            DeliveryCreated event = new DeliveryCreated(
                command.getOrderId(),
                0,
                false
            );
            kafkaTemplate.send("delivery-created", event);
            System.out.println("delivery 이벤트 생성 실패");
        }
    }
}


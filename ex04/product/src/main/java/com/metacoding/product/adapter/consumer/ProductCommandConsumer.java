package com.metacoding.product.adapter.consumer;

import com.metacoding.product.adapter.message.*;
import com.metacoding.product.usecase.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductCommandConsumer {
    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Transactional
    @KafkaListener(topics = "decrease-product-command", groupId = "product-service")
    public void handleDecreaseProductCommand(DecreaseProductCommand command) {
        try {
            // 상품 차감 처리 
            productService.decreaseQuantity(command.getProductId(), command.getQuantity());
            
            // 성공 이벤트 발행
            ProductDecreased event = new ProductDecreased(
                command.getOrderId(),
                command.getProductId(),
                command.getQuantity(),
                true
            );
            kafkaTemplate.send("product-decreased", event);
            System.out.println("product 이벤트 생성 성공");
        } catch (Exception e) {
            // 실패 이벤트 발행
            ProductDecreased event = new ProductDecreased(
                command.getOrderId(),
                command.getProductId(),
                command.getQuantity(),
                false
            );
            kafkaTemplate.send("product-decreased", event);
            System.out.println("product 이벤트 생성 실패");
        }
    }
    
    @Transactional
    @KafkaListener(topics = "increase-product-command", groupId = "product-service")
    public void handleIncreaseProductCommand(IncreaseProductCommand command) {
        try {
            // 상품 재고 복구 처리
            productService.increaseQuantity(command.getProductId(), command.getQuantity());
            System.out.println("product 재고 복구 성공");
        } catch (Exception e) {
            System.out.println("product 재고 복구 실패");
            // 보상 트랜잭션 실패는 심각한 문제이므로 로깅만 수행
            // 필요시 알림 시스템에 전송할 수 있음
        }
    }
}




package com.metacoding.orchestrator.handler;

import com.metacoding.orchestrator.message.*;
import lombok.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class OrderOrchestrator {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<Integer, WorkflowState> workflowStates = new ConcurrentHashMap<>();
    
    @KafkaListener(topics = "order-created", groupId = "orchestrator-service")
    public void handleOrderCreated(OrderCreated event) {
        // 워크플로우 시작: 상태 저장 후 상품 차감 명령 발행
        WorkflowState state = new WorkflowState(
            event.getOrderId(),
            event.getProductId(),
            event.getQuantity(),
            event.getAddress()
        );
        workflowStates.put(event.getOrderId(), state);
        
        sendDecreaseProductCommand(event.getOrderId(), event.getProductId(), event.getQuantity());
    }
    
    @KafkaListener(topics = "product-decreased", groupId = "orchestrator-service")
    public void handleProductDecreased(ProductDecreased event) {
        WorkflowState state = getStateOrReturn(event.getOrderId());
        if (state == null) return;
        
        if (event.isSuccess()) {
            state.setProductDecreased(true);
            sendCreateDeliveryCommand(event.getOrderId(), state.getAddress());
        } else {
            // 상품 차감 실패 → 워크플로우 종료 (보상 불필요)
            workflowStates.remove(event.getOrderId());
        }
    }
    
    @KafkaListener(topics = "delivery-created", groupId = "orchestrator-service")
    public void handleDeliveryCreated(DeliveryCreated event) {
        WorkflowState state = getStateOrReturn(event.getOrderId());
        if (state == null) return;
        
        if (event.isSuccess()) {
            state.setDeliveryCreated(true);
            // 배달 생성 후 즉시 주문 완료하지 않고 대기 상태로 유지
            // 주문 완료는 배달 완료 이벤트에서 처리
        } else {
            // 배달 생성 실패 → 보상 트랜잭션
            compensateProductIfNeeded(state);
            workflowStates.remove(event.getOrderId());
        }
    }
    
    @KafkaListener(topics = "delivery-completed", groupId = "orchestrator-service")
    public void handleDeliveryCompleted(DeliveryCompleted event) {
        WorkflowState state = getStateOrReturn(event.getOrderId());
        if (state == null) return;
        
        if (event.isSuccess()) {
            state.setDeliveryCompleted(true);
            // 배달 완료 시 주문 완료 처리
            if (state.isAllCompleted()) {
                sendCompleteOrderCommand(event.getOrderId());
                workflowStates.remove(event.getOrderId());
            }
        } else {
            // 배달 완료 실패 처리 (필요시 로깅 또는 알림)
            System.out.println("배달 완료 실패: orderId=" + event.getOrderId());
        }
    }
    
    private WorkflowState getStateOrReturn(int orderId) {
        return workflowStates.get(orderId);
    }
    
    private void sendDecreaseProductCommand(int orderId, int productId, int quantity) {
        kafkaTemplate.send("decrease-product-command", 
            new DecreaseProductCommand(orderId, productId, quantity));
    }
    
    private void sendCreateDeliveryCommand(int orderId, String address) {
        kafkaTemplate.send("create-delivery-command", 
            new CreateDeliveryCommand(orderId, address));
    }
    
    private void sendCompleteOrderCommand(int orderId) {
        kafkaTemplate.send("complete-order-command", 
            new CompleteOrderCommand(orderId));
    }
    
    private void compensateProductIfNeeded(WorkflowState state) {
        if (state.isProductDecreased()) {
            kafkaTemplate.send("increase-product-command",
                new IncreaseProductCommand(state.getOrderId(), state.getProductId(), state.getQuantity()));
        }
    }
    
    // 상태 관리
    @Data
    @RequiredArgsConstructor
    private static class WorkflowState {
        private final int orderId;
        private final int productId;
        private final int quantity;
        private final String address;
        private boolean productDecreased = false;
        private boolean deliveryCreated = false;
        private boolean deliveryCompleted = false;
        
        public boolean isAllCompleted() {
            return productDecreased && deliveryCreated && deliveryCompleted;
        }
    }
}
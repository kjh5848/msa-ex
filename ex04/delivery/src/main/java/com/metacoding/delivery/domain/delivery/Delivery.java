package com.metacoding.delivery.domain.delivery;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import com.metacoding.delivery.core.handler.ex.Exception400;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "delivery_tb")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int orderId;
    private String address;
    
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    private Delivery(int orderId, String address, DeliveryStatus status) {
        this.orderId = orderId;
        this.address = address;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Delivery create(int orderId, String address) {
        return new Delivery(orderId, address, DeliveryStatus.PENDING);
    }
    
    public void complete() {
        this.status = DeliveryStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        this.status = DeliveryStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void validateOrderId(int orderId) {
        if (this.orderId != orderId) {
            throw new Exception400("주문 ID가 일치하지 않습니다.");
        }
    }
}













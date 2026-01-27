package com.metacoding.order.domain.order;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import com.metacoding.order.core.handler.ex.Exception400;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "order_tb")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int userId;
    private int productId;
    private int quantity;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    private Order(int userId, int productId, int quantity, OrderStatus status) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Order create(int userId, int productId, int quantity) {
        return new Order(userId, productId, quantity, OrderStatus.PENDING);
    }

    public void complete() {
        this.status = OrderStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        // 이미 취소된 주문인지 확인
        if (this.status == OrderStatus.CANCELLED) {
            throw new Exception400("이미 취소된 주문입니다.");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
}














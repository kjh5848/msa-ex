package com.metacoding.order.domain.order;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import com.metacoding.order.core.handler.ex.Exception400;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "order_item_tb")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int orderId;
    private int productId;
    private int quantity;
    private Long price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    private OrderItem(int orderId, int productId, int quantity, Long price) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static OrderItem create(int orderId, int productId, int quantity, Long price) {
        return new OrderItem(orderId, productId, quantity, price);
    }

    public void validatePrice(Long price) {
        if (!this.price.equals(price)) {
            throw new Exception400("가격이 일치하지 않습니다.");
        }
    }
}










package com.metacoding.product.domain.product;

import com.metacoding.product.core.handler.ex.Exception400;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "product_tb")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String productName;
    private int quantity;
    private Long price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    private Product(String productName, int quantity, Long price) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Product create(String productName, int quantity, Long price) {
        return new Product(productName, quantity, price);
    }
    
    public void decreaseQuantity(int quantity) {
        if (this.quantity < quantity) {
            throw new Exception400("제품의 수량이 부족합니다.");
        }
        this.quantity -= quantity;
    }
    public void increaseQuantity(int quantity) {
        if (quantity <= 0) {
            throw new Exception400("복구할 수량은 0보다 커야 합니다.");
        }
        this.quantity += quantity;
    }
}













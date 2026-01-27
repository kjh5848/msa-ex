package com.metacoding.order.orders;

import java.time.LocalDateTime;

public class OrderResponse {
    public record DTO(
        int id,
        int userId,
        int productId,
        int quantity,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        public DTO(Order order) {
            this(order.getId(), order.getUserId(), order.getProductId(), order.getQuantity(), order.getStatus(), order.getCreatedAt(), order.getUpdatedAt());
        }
    }
}

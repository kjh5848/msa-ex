package com.metacoding.delivery.deliveries;

import java.time.LocalDateTime;

public class DeliveryResponse {
    public record DTO(
    int id,
    int orderId,
    String address,
    DeliveryStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
    ) {
        public DTO(Delivery delivery) {
            this(delivery.getId(), delivery.getOrderId(), delivery.getAddress(), delivery.getStatus(), delivery.getCreatedAt(), delivery.getUpdatedAt());
        }
    }
}

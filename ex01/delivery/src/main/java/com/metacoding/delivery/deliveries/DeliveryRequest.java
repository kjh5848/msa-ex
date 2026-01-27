package com.metacoding.delivery.deliveries;

public class DeliveryRequest {
    public record SaveDTO(
        int orderId,
        String address
    ) {
    }
}

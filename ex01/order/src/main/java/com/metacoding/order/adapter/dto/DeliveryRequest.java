package com.metacoding.order.adapter.dto;

public class DeliveryRequest {
    public record SaveDTO(
        int orderId,
        String address
    ) {
    }
}


package com.metacoding.order.orders;

public class OrderRequest {
    public record SaveDTO(
        int productId,
        int quantity,
        Long price,
        String address
    ) {
    }
}
package com.metacoding.order.web.dto;

public record CreateOrderRequest(
    int productId,
    int quantity,
    Long price,
    String address
) {
}





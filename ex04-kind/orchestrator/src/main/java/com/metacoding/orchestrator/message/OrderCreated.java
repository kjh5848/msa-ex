package com.metacoding.orchestrator.message;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreated {
    private int orderId;
    private int userId;
    private int productId;
    private int quantity;
    private Long price;
    private String address;
}












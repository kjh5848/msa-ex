package com.metacoding.orchestrator.message;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDecreased {
    private int orderId;
    private int productId;
    private int quantity;
    private boolean success;
}



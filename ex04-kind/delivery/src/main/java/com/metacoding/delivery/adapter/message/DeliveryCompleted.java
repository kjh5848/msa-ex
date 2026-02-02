package com.metacoding.delivery.adapter.message;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryCompleted {
    private int orderId;
    private int deliveryId;
    private boolean success;
}

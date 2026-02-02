package com.metacoding.delivery.adapter.message;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompleteDeliveryCommand {
    private int orderId;
}

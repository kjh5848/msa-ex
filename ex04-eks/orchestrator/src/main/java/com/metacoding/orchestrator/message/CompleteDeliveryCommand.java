package com.metacoding.orchestrator.message;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompleteDeliveryCommand {
    private int orderId;
}

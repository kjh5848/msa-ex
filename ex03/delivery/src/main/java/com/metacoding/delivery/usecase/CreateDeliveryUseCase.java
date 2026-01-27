package com.metacoding.delivery.usecase;

import com.metacoding.delivery.web.dto.DeliveryResponse;

public interface CreateDeliveryUseCase {
    DeliveryResponse saveDelivery(int orderId, String address);
}




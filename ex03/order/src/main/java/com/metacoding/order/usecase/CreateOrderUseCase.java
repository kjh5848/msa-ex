package com.metacoding.order.usecase;

import com.metacoding.order.web.dto.OrderResponse;

public interface CreateOrderUseCase {
    OrderResponse createOrder(int userId, int productId, int quantity, Long price, String address);
}



package com.metacoding.order.usecase;

import com.metacoding.order.domain.order.*;
import com.metacoding.order.repository.*;
import com.metacoding.order.adapter.producer.OrderEventProducer;
import com.metacoding.order.adapter.message.OrderCreated;
import com.metacoding.order.core.handler.ex.*;
import com.metacoding.order.web.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class OrderService implements CreateOrderUseCase, CancelOrderUseCase, GetOrderUseCase {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEventProducer orderEventProducer;

    @Override
    @Transactional
    public OrderResponse createOrder(int userId, int productId, int quantity, Long price, String address) {
        // 1. 주문 생성 (PENDING 상태)
        Order order = Order.create(userId, productId, quantity);
        Order savedOrder = orderRepository.save(order);

        // 2. 주문 아이템 생성
        OrderItem orderItem = OrderItem.create(
            savedOrder.getId(),
            productId,
            quantity,
            price
        );
        orderItem.validatePrice(price);
        orderItemRepository.save(orderItem);

        // 3. OrderCreated 이벤트 발행 (알림)
        OrderCreated event = new OrderCreated(
            savedOrder.getId(),
            userId,
            productId,
            quantity,
            price,
            address
        );
        orderEventProducer.publishOrderCreated(event);

        return OrderResponse.from(savedOrder);
    }

    @Override
    public OrderResponse getOrder(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new Exception404("주문을 찾을 수 없습니다."));
        return OrderResponse.from(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(int orderId) {
        // 1. 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new Exception404("주문을 찾을 수 없습니다."));

        // 2. 주문 취소
        order.cancel();

        return OrderResponse.from(order);
    }
}


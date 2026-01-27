package com.metacoding.order.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    Optional<OrderItem> findByOrderId(int orderId);
    List<OrderItem> findAllByOrderId(int orderId);
}


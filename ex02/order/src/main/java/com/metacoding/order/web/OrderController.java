package com.metacoding.order.web;

import com.metacoding.order.usecase.*;
import com.metacoding.order.web.dto.*;
import com.metacoding.order.core.util.Resp;
import com.metacoding.order.core.handler.ex.Exception401;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    @PostMapping
    public ResponseEntity<?> saveOrder(@RequestBody CreateOrderRequest requestDTO, HttpServletRequest request) {
        // Gateway에서 전달한 userId 사용
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new Exception401("인증이 필요합니다");
        }
        
        OrderResponse response = createOrderUseCase.createOrder(userId,requestDTO.productId(),requestDTO.quantity(),requestDTO.price(),requestDTO.address());
        return Resp.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable("orderId") int orderId) {
        OrderResponse response = getOrderUseCase.getOrder(orderId);
        return Resp.ok(response);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable("orderId") int orderId) {
        OrderResponse response = cancelOrderUseCase.cancelOrder(orderId);
        return Resp.ok(response);
    }
}


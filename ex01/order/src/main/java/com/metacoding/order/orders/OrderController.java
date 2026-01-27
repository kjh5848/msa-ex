package com.metacoding.order.orders;

import org.springframework.http.ResponseEntity;
import com.metacoding.order.core.util.Resp;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> saveOrder(@RequestBody OrderRequest.SaveDTO requestDTO, @RequestAttribute("userId") Integer userId) {
        return Resp.ok(orderService.createOrder(userId, requestDTO.productId(), requestDTO.quantity(), requestDTO.price(), requestDTO.address()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable("orderId") int orderId) {
        return Resp.ok(orderService.findById(orderId));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable("orderId") int orderId) {
        return Resp.ok(orderService.cancelOrder(orderId));
    }
}

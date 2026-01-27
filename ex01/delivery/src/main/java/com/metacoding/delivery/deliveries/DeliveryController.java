package com.metacoding.delivery.deliveries;

import org.springframework.http.ResponseEntity;
import com.metacoding.delivery.core.util.Resp;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {
    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<?> saveDelivery(@RequestBody DeliveryRequest.SaveDTO requestDTO) {
        return Resp.ok(deliveryService.saveDelivery(requestDTO.orderId(), requestDTO.address()));
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<?> getDelivery(@PathVariable("deliveryId") int deliveryId) {
        return Resp.ok(deliveryService.findById(deliveryId));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> cancelDelivery(@PathVariable("orderId") int orderId) {
        deliveryService.cancelDelivery(orderId);
        return Resp.ok(null);
    }
}

package com.metacoding.delivery.web;

import com.metacoding.delivery.usecase.*;
import com.metacoding.delivery.web.dto.*;
import com.metacoding.delivery.core.util.Resp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/deliveries")
public class DeliveryController {
    private final SaveDeliveryUseCase saveDeliveryUseCase;
    private final GetDeliveryUseCase getDeliveryUseCase;
    private final CancelDeliveryUseCase cancelDeliveryUseCase;

    @PostMapping
    public ResponseEntity<?> saveDelivery(@RequestBody CreateDeliveryRequest requestDTO) {
        DeliveryResponse response = saveDeliveryUseCase.saveDelivery(requestDTO.orderId(),requestDTO.address());
        return Resp.ok(response);
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<?> getDelivery(@PathVariable("deliveryId") int deliveryId) {
        DeliveryResponse response = getDeliveryUseCase.findById(deliveryId);
        return Resp.ok(response);
    }

    @DeleteMapping("/{deliveryId}")
    public ResponseEntity<?> cancelDelivery(@PathVariable("deliveryId") int deliveryId) {
        cancelDeliveryUseCase.cancelDelivery(deliveryId);
        return Resp.ok(null);
    }
}



















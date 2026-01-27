package com.metacoding.order.adapter;

import com.metacoding.order.adapter.dto.DeliveryRequest;
import com.metacoding.order.core.util.Resp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "delivery-service", url = "http://delivery-service:8084")
public interface DeliveryClient {
        
    @PostMapping("/deliveries")
    void saveDelivery(@RequestBody DeliveryRequest.SaveDTO request);
    
    @DeleteMapping("/deliveries/{orderId}")
    void cancelDelivery(@PathVariable("orderId") int orderId);
}


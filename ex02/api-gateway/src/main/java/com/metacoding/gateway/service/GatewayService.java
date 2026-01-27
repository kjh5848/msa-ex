package com.metacoding.gateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GatewayService {

    private final RestTemplate restTemplate;

    @Value("${gateway.services.order:http://order-service:8081}")
    private String orderServiceUrl;

    @Value("${gateway.services.product:http://product-service:8082}")
    private String productServiceUrl;

    @Value("${gateway.services.user:http://user-service:8083}")
    private String userServiceUrl;

    @Value("${gateway.services.delivery:http://delivery-service:8084}")
    private String deliveryServiceUrl;
    
    // 요청 전달
    public ResponseEntity<String> forwardRequest(String serviceType, String path, HttpMethod method, HttpHeaders headers, String body) {
        String targetUrl = getServiceUrl(serviceType) + path;
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(targetUrl, method, entity, String.class);
    }

    // 서비스 타입에 따라 서비스 URL 반환
    private String getServiceUrl(String serviceType) {
        return switch (serviceType) {
            case "order" -> orderServiceUrl;
            case "user" -> userServiceUrl;
            case "product" -> productServiceUrl;
            case "delivery" -> deliveryServiceUrl;
            default -> throw new IllegalArgumentException("Unknown service: " + serviceType);
        };
    }
}

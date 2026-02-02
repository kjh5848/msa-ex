package com.metacoding.gateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

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
        //return restTemplate.exchange(targetUrl, method, entity, String.class);
        ResponseEntity<String> response = restTemplate.exchange(targetUrl, method, entity, String.class);
        
        // 헤더 정리: Transfer-Encoding 제거, Content-Length 설정
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.putAll(response.getHeaders());
        responseHeaders.remove("Transfer-Encoding");
        
        // 본문이 있으면 Content-Length 설정
        String responseBody = response.getBody();
        if (responseBody != null) {
            responseHeaders.setContentLength(responseBody.getBytes(StandardCharsets.UTF_8).length);
        }       
        return new ResponseEntity<>(responseBody, responseHeaders, response.getStatusCode());
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

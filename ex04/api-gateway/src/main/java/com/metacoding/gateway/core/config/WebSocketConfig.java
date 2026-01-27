package com.metacoding.gateway.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

// 웹소켓 요청 처리를 위한 설정
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final WebSocketHandshakeInterceptor handshakeInterceptor;
    
    // order 서비스 URL
    @Value("${gateway.services.order:http://order-service:8081}")
    private String orderServiceUrl;
    
    public WebSocketConfig(WebSocketHandshakeInterceptor handshakeInterceptor) {
        this.handshakeInterceptor = handshakeInterceptor;
    }
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // SockJS 클라이언트는 HTTP URL을 사용 (자동으로 ws://로 변환)
        String httpUrl = orderServiceUrl + "/ws/orders";
        // httpUrl은 백엔드 연결 주소, "/api/ws/orders"는 클라이언트의 연결을 위한 주소
        registry.addHandler(new WebSocketProxyConfig(java.net.URI.create(httpUrl)), "/api/ws/orders") 
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor)  // 인터셉터 추가
                .withSockJS(); // SockJS를 사용하여 웹소켓 연결
    }
}

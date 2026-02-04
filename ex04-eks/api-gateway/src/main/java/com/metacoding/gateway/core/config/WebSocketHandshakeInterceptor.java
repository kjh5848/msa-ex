package com.metacoding.gateway.core.config;

import com.metacoding.gateway.core.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    
    private final JwtUtil jwtUtil;
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 쿼리 파라미터에서 토큰 추출 (SockJS는 헤더를 직접 설정할 수 없음)
        String token = null;
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    token = param.substring(6);
                    // "Bearer " 제거 (있을 경우)
                    if (token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    }
                    break;
                }
            }
        }
        
        // 토큰 검증 및 userId 추출
        if (token != null && jwtUtil.validateToken(token)) {
            try {
                int userId = jwtUtil.getUserId(token);
                attributes.put("userId", userId);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        
        return false; // 토큰이 없거나 유효하지 않으면 연결 거부
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
    }
}

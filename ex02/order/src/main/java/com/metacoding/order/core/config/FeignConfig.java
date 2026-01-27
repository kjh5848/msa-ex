package com.metacoding.order.core.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.*;

@Component
public class FeignConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // 1. 먼저 X-User-Id 헤더 확인
            String userIdHeader = request.getHeader("X-User-Id");
            
            // 2. 헤더가 없으면 request attribute에서 userId 확인
            if (userIdHeader == null || userIdHeader.isEmpty()) {
                Integer userId = (Integer) request.getAttribute("userId");
                if (userId != null) {
                    userIdHeader = String.valueOf(userId);
                }
            }
            
            // 3. userId가 있으면 헤더에 추가
            if (userIdHeader != null && !userIdHeader.isEmpty()) {
                template.header("X-User-Id", userIdHeader);
            }
        }
    }
}


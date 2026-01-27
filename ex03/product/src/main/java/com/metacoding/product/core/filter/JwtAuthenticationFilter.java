package com.metacoding.product.core.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        // X-User-Id 헤더만 검증 (토큰 인증 제거)
        String userIdHeader = request.getHeader("X-User-Id");
        
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다");
            return;
        }
        
        try {
            Integer userId = Integer.parseInt(userIdHeader);
            request.setAttribute("userId", userId);
            filterChain.doFilter(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 사용자 ID입니다");
            return;
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/login") ||
               path.startsWith("/h2-console");
    }
}

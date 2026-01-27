package com.metacoding.gateway.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.sockjs.client.*;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class WebSocketProxyConfig extends TextWebSocketHandler {

    private final URI backendUri;
    private final Map<String, WebSocketSession> backendByClientId = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession client) throws Exception {
        // 세션 attributes에서 userId 가져오기
        Integer userId = (Integer) client.getAttributes().get("userId");
        
        // userId가 없으면 연결 거부
        if (userId == null) {
            client.close(CloseStatus.POLICY_VIOLATION.withReason("Authentication required"));
            return;
        }
        
        List<Transport> transports = Arrays.asList(
            new WebSocketTransport(new StandardWebSocketClient()),
            new RestTemplateXhrTransport()
        );
        SockJsClient sockJsClient = new SockJsClient(transports);
        
        WebSocketSession backend = sockJsClient.doHandshake(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
                if (client.isOpen()) client.sendMessage(msg);
            }
            
            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                if (client.isOpen()) client.close(status);
            }
        }, null, backendUri).get();

        backendByClientId.put(client.getId(), backend);
    }

    @Override
    protected void handleTextMessage(WebSocketSession client, TextMessage msg) throws Exception {
        WebSocketSession backend = backendByClientId.get(client.getId());
        if (backend != null && backend.isOpen()) backend.sendMessage(msg);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession client, CloseStatus status) throws Exception {
        WebSocketSession backend = backendByClientId.remove(client.getId());
        if (backend != null && backend.isOpen()) backend.close(status);
    }
}

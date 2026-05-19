package com.property.ws;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/work-order/{orderId}")
public class WorkOrderEndpoint {

    private static final Map<String, Set<Session>> SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("orderId") String orderId) {
        SESSIONS.computeIfAbsent(orderId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.debug("WS opened orderId={} sessionId={}", orderId, session.getId());
    }

    @OnClose
    public void onClose(Session session, @PathParam("orderId") String orderId) {
        Set<Session> set = SESSIONS.get(orderId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) SESSIONS.remove(orderId);
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {
        log.warn("WS error sessionId={}: {}", session.getId(), t.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
    }

    public static void broadcast(String orderId, String json) {
        Set<Session> set = SESSIONS.getOrDefault(orderId, Set.of());
        for (Session s : set) {
            if (s.isOpen()) {
                s.getAsyncRemote().sendText(json);
            }
        }
    }
}

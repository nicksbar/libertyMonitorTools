package com.github.nickbar.LibertyUatTools.Endpoints.Performance;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/telemetry")
@ApplicationScoped
public class TelemetryWebSocket {

    @Inject
    private SystemPerformanceInfo systemPerformanceInfo;

    @OnOpen
    public void onOpen(Session session) {
        systemPerformanceInfo.addSession(session);
    }

    @OnClose
    public void onClose(Session session) {
        systemPerformanceInfo.removeSession(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        systemPerformanceInfo.removeSession(session);
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // Handle incoming messages if needed
    }
}

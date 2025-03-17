package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * websocket service
 */
@ServerEndpoint(value = "/chatServer/{userId}")
@Component
public class WebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);
    /**
     * Record the current number of online connections
     */
    public static final Map<Integer, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * Method called when a connection is successfully established
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Integer userId) {
        sessionMap.put(userId, session);
        log.info("A new user has joined, userId={}, the current number of online users is: {}", userId, sessionMap.size());
    }

    /**
     * Method called when a connection is closed
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") Integer userId) {
        sessionMap.remove(userId);
        log.info("A connection has been closed, the userId={} user session has been removed, the current number of online users is: {}", userId, sessionMap.size());
    }

    /**
     * Method called when a message is received from the client
     * The backend receives the message sent by the client
     * onMessage is a message relay
     * Accept json data sent by browser socket.send
     *
     */
    @OnMessage
    public void onMessage(String message) {
        for (Session session : sessionMap.values()) {
            log.info("The server sends a message to the client [{}] {}", session.getId(), message);
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.error("Socket sending message failed", e);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("Socket error", error);
    }
}



package com.vibe.im.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket connection manager for managing active WebSocket sessions.
 * Provides functionality to add, remove, and manage user connections with heartbeat timeout detection.
 *
 * @author Claude
 */
@Component
public class ConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    /**
     * Map of sessionId to UserConnection for all active connections.
     * Uses ConcurrentHashMap for thread safety in multi-threaded environment.
     */
    private static final ConcurrentHashMap<String, UserConnection> connections = new ConcurrentHashMap<>();

    /**
     * Map of userId to sessionId for quick user lookup.
     * Allows checking if a user is online and retrieving their session.
     */
    private static final ConcurrentHashMap<Long, String> userIdToSessionId = new ConcurrentHashMap<>();

    /**
     * Scheduled executor for periodic heartbeat timeout checking.
     * Runs every 30 seconds to detect and cleanup stale connections.
     */
    private static final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "websocket-heartbeat-checker");
        t.setDaemon(true);
        return t;
    });

    /**
     * Heartbeat timeout threshold in milliseconds (30 seconds).
     * Connections without heartbeat update for this duration will be removed.
     */
    private static final long HEARTBEAT_TIMEOUT_MS = 30000;

    /**
     * Heartbeat check interval in milliseconds (30 seconds).
     */
    private static final long HEARTBEAT_CHECK_INTERVAL_MS = 30000;

    // Static initialization block to schedule heartbeat checking
    static {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                connections.forEach((sessionId, connection) -> {
                    if (currentTime - connection.getLastHeartbeat() > HEARTBEAT_TIMEOUT_MS) {
                        logger.info("Connection timed out for userId: {}, sessionId: {}",
                                connection.getUserId(), sessionId);
                        try {
                            connection.getSession().close();
                            removeConnection(sessionId);
                        } catch (Exception e) {
                            logger.error("Failed to close timed out connection for sessionId: {}", sessionId, e);
                        }
                    }
                });
            } catch (Exception e) {
                logger.error("Error during heartbeat check", e);
            }
        }, HEARTBEAT_CHECK_INTERVAL_MS, HEARTBEAT_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);

        logger.info("ConnectionManager initialized with heartbeat checker (interval: {}ms)", HEARTBEAT_CHECK_INTERVAL_MS);
    }

    /**
     * Adds a new WebSocket connection to the manager.
     *
     * @param userId   the user ID associated with this connection
     * @param sessionId the WebSocket session ID
     * @param session  the WebSocket session
     */
    public static void addConnection(Long userId, String sessionId, WebSocketSession session) {
        UserConnection connection = new UserConnection(userId, sessionId, session);
        connections.put(sessionId, connection);
        userIdToSessionId.put(userId, sessionId);
        logger.info("Connection added for userId: {}, sessionId: {}", userId, sessionId);
    }

    /**
     * Removes a WebSocket connection from the manager.
     *
     * @param sessionId the WebSocket session ID to remove
     */
    public static void removeConnection(String sessionId) {
        UserConnection connection = connections.remove(sessionId);
        if (connection != null) {
            userIdToSessionId.remove(connection.getUserId());
            logger.info("Connection removed for userId: {}, sessionId: {}", connection.getUserId(), sessionId);
        }
    }

    /**
     * Retrieves a connection by session ID.
     *
     * @param sessionId the WebSocket session ID
     * @return the UserConnection, or null if not found
     */
    public static UserConnection getConnection(String sessionId) {
        return connections.get(sessionId);
    }

    /**
     * Retrieves a connection by user ID.
     *
     * @param userId the user ID
     * @return the UserConnection, or null if not found
     */
    public static UserConnection getConnectionByUserId(Long userId) {
        String sessionId = userIdToSessionId.get(userId);
        return sessionId != null ? connections.get(sessionId) : null;
    }

    /**
     * Checks if a user is currently online.
     *
     * @param userId the user ID to check
     * @return true if the user is online, false otherwise
     */
    public static boolean isUserOnline(Long userId) {
        return userIdToSessionId.containsKey(userId);
    }

    /**
     * Sends a text message to a specific user.
     *
     * @param userId  the user ID to send the message to
     * @param message the message content
     * @throws Exception if the message sending fails
     */
    public static void sendMessageToUser(Long userId, String message) throws Exception {
        UserConnection connection = getConnectionByUserId(userId);
        if (connection != null) {
            connection.getSession().sendMessage(new TextMessage(message));
            logger.debug("Message sent to userId: {}, length: {}", userId, message.length());
        } else {
            logger.warn("Failed to send message: user {} is not online", userId);
        }
    }

    /**
     * Updates the heartbeat timestamp for a connection.
     *
     * @param sessionId the WebSocket session ID
     */
    public static void updateHeartbeat(String sessionId) {
        UserConnection connection = connections.get(sessionId);
        if (connection != null) {
            connection.setLastHeartbeat(System.currentTimeMillis());
            logger.debug("Heartbeat updated for sessionId: {}", sessionId);
        }
    }

    /**
     * Gets the current number of active connections.
     *
     * @return the number of active connections
     */
    public static int getConnectionCount() {
        return connections.size();
    }

    /**
     * Inner class representing a user's WebSocket connection.
     * Contains all necessary information for managing a single connection.
     */
    public static class UserConnection {
        private final Long userId;
        private final String sessionId;
        private final WebSocketSession session;
        private volatile long lastHeartbeat;

        /**
         * Creates a new UserConnection.
         *
         * @param userId    the user ID
         * @param sessionId the WebSocket session ID
         * @param session   the WebSocket session
         */
        public UserConnection(Long userId, String sessionId, WebSocketSession session) {
            this.userId = userId;
            this.sessionId = sessionId;
            this.session = session;
            this.lastHeartbeat = System.currentTimeMillis();
        }

        public Long getUserId() {
            return userId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public WebSocketSession getSession() {
            return session;
        }

        public long getLastHeartbeat() {
            return lastHeartbeat;
        }

        public void setLastHeartbeat(long lastHeartbeat) {
            this.lastHeartbeat = lastHeartbeat;
        }
    }
}

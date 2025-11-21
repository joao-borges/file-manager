package br.com.joaoborges.filemanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration
 *
 * Configures WebSocket support for real-time progress updates during file operations.
 * Uses STOMP protocol over WebSocket for message communication.
 *
 * Architecture:
 * - STOMP endpoint: /ws - Entry point for WebSocket connections
 * - Message broker: /topic - Broadcast messages to subscribed clients
 * - Application prefix: /app - Client-to-server messages
 *
 * Usage:
 * - Clients connect to: ws://localhost:8080/ws
 * - Subscribe to: /topic/progress/{operationId}
 * - Receive progress updates in real-time
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker for pub/sub messaging
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory broker for /topic destinations
        config.enableSimpleBroker("/topic");

        // Set application destination prefix for client messages
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Register STOMP endpoints for WebSocket connections
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register /ws endpoint with SockJS fallback support
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins for development
                .withSockJS(); // Enable SockJS fallback for browsers without WebSocket support
    }
}

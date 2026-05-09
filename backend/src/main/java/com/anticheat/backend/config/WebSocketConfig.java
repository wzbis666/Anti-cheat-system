package com.anticheat.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.anticheat.backend.handler.CheatWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CheatWebSocketHandler cheatWebSocketHandler;

    @Autowired
    public WebSocketConfig(CheatWebSocketHandler cheatWebSocketHandler) {
        this.cheatWebSocketHandler = cheatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(cheatWebSocketHandler, "/ws/cheats")
                .setAllowedOrigins(
                    "http://localhost:3000", "http://localhost:3001", "http://localhost:3030",
                    "http://127.0.0.1:3000", "http://127.0.0.1:3001", "http://127.0.0.1:3030"
                );
    }
}

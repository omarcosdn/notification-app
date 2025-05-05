package com.github.omarcosdn.notification.infrastructure.config;

import com.github.omarcosdn.notification.infrastructure.web.socket.WebSocketServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private final WebSocketServer webSocketServer;

  public WebSocketConfig(final WebSocketServer webSocketServer) {
    this.webSocketServer = webSocketServer;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(webSocketServer, "/ws/messages").setAllowedOrigins("*");
  }
}

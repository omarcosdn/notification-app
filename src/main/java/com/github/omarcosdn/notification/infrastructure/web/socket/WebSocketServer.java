package com.github.omarcosdn.notification.infrastructure.web.socket;

import com.github.omarcosdn.notification.core.repositories.MessageRepository;
import com.github.omarcosdn.notification.shared.utils.ObjectMapperHolder;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketServer extends TextWebSocketHandler {

  private final Map<UUID, WebSocketSession> tenantSessions = new ConcurrentHashMap<>();
  private final Map<WebSocketSession, UUID> sessionTenants = new ConcurrentHashMap<>();

  private final MessageRepository repository;

  public WebSocketServer(final MessageRepository repository) {
    this.repository = Objects.requireNonNull(repository);
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    //
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    UUID tenantId = sessionTenants.remove(session);
    if (tenantId != null) {
      tenantSessions.remove(tenantId);
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    var json = ObjectMapperHolder.readTree(message.getPayload());
    var type = json.get("type").asText();

    switch (type) {
      case "auth":
        handleAuth(session, UUID.fromString(json.get("tenantId").asText()));
        break;
      case "ack":
        handleAck(UUID.fromString(json.get("messageId").asText()));
        break;
      default:
        session.close(CloseStatus.BAD_DATA);
    }
  }

  private void handleAck(final UUID messageId) {
    repository.markMessageAsAcked(messageId);
  }

  private void handleAuth(WebSocketSession session, UUID tenantId) throws IOException {
    if (tenantSessions.containsKey(tenantId)) {
      var error = ObjectMapperHolder.createObjectNode();
      error.put("type", "error");
      error.put("message", "Tenant already connected");

      session.sendMessage(new TextMessage(error.toString()));
      session.close(CloseStatus.POLICY_VIOLATION);
      return;
    }

    //improve for scalability
    tenantSessions.put(tenantId, session);
    sessionTenants.put(session, tenantId);

    var unAckedMessages = repository.findUnAckedByTenantId(tenantId);
    for (var message : unAckedMessages) {
      var out = ObjectMapperHolder.createObjectNode();
      out.put("type", "message");
      out.put("messageId", message.identity().toString());
      out.put("content", message.getContent());
      session.sendMessage(new TextMessage(out.toString()));
    }
  }

  public boolean hasTenantSession(final UUID tenantId) {
    var session = tenantSessions.get(tenantId);
    return session != null && session.isOpen();
  }

  public void sendToTenant(final UUID tenantId, UUID messageId, String content) throws IOException {
    var session = tenantSessions.get(tenantId);
    if (session != null && session.isOpen()) {
      var json = ObjectMapperHolder.createObjectNode();
      json.put("type", "message");
      json.put("messageId", messageId.toString());
      json.put("content", content);

      session.sendMessage(new TextMessage(json.toString()));
    }
  }
}

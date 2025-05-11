package com.github.omarcosdn.notification.infrastructure.websocket;

import com.github.omarcosdn.notification.core.repositories.MessageRepository;
import com.github.omarcosdn.notification.shared.utils.ObjectMapperHolder;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {

  private static final String TENANT_HEADER = "Tenant-Id";
  private static final String ERROR_TYPE = "error";
  private static final String ACK_TYPE = "ack";

  private final MessageRepository messageRepository;
  private final WebSocketSessionManager sessionManager;

  public WebSocketHandler(final MessageRepository repository, final WebSocketSessionManager sessionManager) {
    this.messageRepository = Objects.requireNonNull(repository);
    this.sessionManager = Objects.requireNonNull(sessionManager);
  }

  @Override
  public void afterConnectionEstablished(@NonNull WebSocketSession session) throws IOException {
    handleTenant(session);
  }

  @Override
  public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
    var tenantId = getTenantFromSession(session);
    if (tenantId != null) {
      sessionManager.unregister(tenantId, session.getId());
    }
  }

  @Override
  public void handlePongMessage(@NonNull WebSocketSession session, @NonNull PongMessage message) {
    var tenantId = getTenantFromSession(session);
    if (tenantId != null) {
      sessionManager.updatePong(tenantId);
    }
  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message)
      throws Exception {
    var tenantId = getTenantFromSession(session);
    if (Objects.isNull(tenantId)) {
      handleInvalidTenant(session);
      return;
    }

    var json = ObjectMapperHolder.readTree(message.getPayload());
    var type = json.get("type").asText();

    if (ACK_TYPE.equals(type)) {
      handleAck(UUID.fromString(json.get("messageId").asText()));
      return;
    }

    session.close(CloseStatus.BAD_DATA);
  }

  private void handleAck(final UUID messageId) {
    messageRepository.markMessageAsAcked(messageId);
  }

  private void handleTenant(final WebSocketSession session) throws IOException {
    var tenantId = getTenantFromSession(session);
    if (Objects.isNull(tenantId)) {
      handleInvalidTenant(session);
      return;
    }

    var sessionRegistered = sessionManager.register(tenantId, session);
    if (Boolean.FALSE.equals(sessionRegistered)) {
      handleExistingConnection(session, tenantId);
      return;
    }

    sendUnAckedMessages(tenantId);
  }

  private void handleInvalidTenant(final WebSocketSession session) throws IOException {
    var error = createErrorResponse("Invalid tenant header");
    session.sendMessage(new TextMessage(error));
    session.close(CloseStatus.POLICY_VIOLATION);
    log.error("Invalid tenant header");
  }

  private void handleExistingConnection(final WebSocketSession session, final UUID tenantId) throws IOException {
    var error = createErrorResponse("Connection already exists for tenant " + tenantId);
    session.sendMessage(new TextMessage(error));
    session.close(CloseStatus.POLICY_VIOLATION);
  }

  private void sendUnAckedMessages(final UUID tenantId) {
    var unAckedMessages = messageRepository.findUnAckedByTenantId(tenantId);
    for (var message : unAckedMessages) {
      sessionManager.sendMessage(message.getTenantId(), message.identity(), message.getContent());
    }
  }

  private UUID getTenantFromSession(final WebSocketSession session) {
    var header = session.getHandshakeHeaders().getFirst(TENANT_HEADER);
    try {
      return header != null ? UUID.fromString(header) : null;
    } catch (IllegalArgumentException e) {
      log.error("Invalid UUID in tenant header: {}", header);
      return null;
    }
  }

  private String createErrorResponse(final String message) {
    var error = ObjectMapperHolder.createObjectNode();
    error.put("type", ERROR_TYPE);
    error.put("message", message);
    return error.toString();
  }
}

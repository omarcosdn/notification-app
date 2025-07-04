package com.github.omarcosdn.notification.core.services.impl;

import com.github.omarcosdn.notification.core.entities.Message;
import com.github.omarcosdn.notification.core.repositories.IdempotencyRepository;
import com.github.omarcosdn.notification.core.repositories.MessageRepository;
import com.github.omarcosdn.notification.core.services.MessageDispatcher;
import com.github.omarcosdn.notification.infrastructure.websocket.WebSocketSessionManager;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DefaultMessageDispatcher implements MessageDispatcher {

  private final MessageRepository messageRepository;
  private final IdempotencyRepository idempotencyRepository;
  private final WebSocketSessionManager sessionManager;

  public DefaultMessageDispatcher(final MessageRepository messageRepository,
                                  final IdempotencyRepository idempotencyRepository,
                                  final WebSocketSessionManager sessionManager) {
    this.messageRepository = Objects.requireNonNull(messageRepository);
    this.idempotencyRepository = Objects.requireNonNull(idempotencyRepository);
    this.sessionManager = Objects.requireNonNull(sessionManager);
  }

  @Override
  public void dispatch(final UUID tenantId, final UUID idempotencyKey, final String content) {
    if (!idempotencyRepository.registerIfAbsent(idempotencyKey)) {
      log.warn("Duplicate message with key {}, skipping.", idempotencyKey);
      return;
    }

    var message = Message.create(tenantId, content);

    messageRepository.upsert(message);

    if (!sessionManager.hasSession(tenantId)) {
      log.debug("No active session for tenant {}, message saved for later delivery", tenantId);
      return;
    }

    log.debug("Sending message {} to tenant {} via WebSocket", message.identity(), tenantId);
    sessionManager.sendMessage(tenantId, message.identity(), message.getContent());
  }
}

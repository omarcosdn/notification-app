package com.github.omarcosdn.notification.infrastructure.websocket;

import com.github.omarcosdn.notification.core.repositories.TenantConnectionRepository;
import com.github.omarcosdn.notification.shared.utils.ObjectMapperHolder;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class WebSocketSessionManager {

  private static final long PING_PONG_RATE_MS = 30000;
  private static final long PONG_TIMEOUT_MS = 60000;

  private final Map<UUID, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
  private final Map<UUID, Instant> pongMap = new ConcurrentHashMap<>();

  private final TenantConnectionRepository repository;

  public WebSocketSessionManager(final TenantConnectionRepository repository) {
    this.repository = Objects.requireNonNull(repository);
  }

  public boolean register(final UUID tenantId, final WebSocketSession session) {
    try {
      var existing = sessionMap.putIfAbsent(tenantId, session);
      if (existing != null) return false;
      repository.register(tenantId);
      pongMap.put(tenantId, Instant.now());
      log.info("Connection established for tenantId: {}", tenantId);
      return true;
    } catch (IllegalStateException e) {
      sessionMap.remove(tenantId);
      log.info("Connection already exists for tenantId: {}", tenantId);
      return false;
    }
  }

  public void unregister(final UUID tenantId) {
    repository.unregister(tenantId);
    sessionMap.remove(tenantId);
    pongMap.remove(tenantId);
    log.info("Session closed for tenant {}", tenantId);
  }

  public void updatePong(final UUID tenantId) {
    log.info("PONG received from tenant {}", tenantId);
    repository.refresh(tenantId);
    pongMap.put(tenantId, Instant.now());
  }

  public void sendMessage(final UUID tenantId, final UUID messageId, final String content) {
    Optional.ofNullable(sessionMap.get(tenantId))
        .filter(WebSocketSession::isOpen)
        .ifPresent(
            session -> {
              try {
                var json = buildMessage(tenantId, messageId, content);
                session.sendMessage(new TextMessage(json));
              } catch (IOException e) {
                log.error("Error sending message to WebSocket for tenantId {}", tenantId, e);
              }
            });
  }

  public Map<UUID, WebSocketSession> getAllSessions() {
    return Map.copyOf(sessionMap);
  }

  public boolean hasSession(final UUID tenantId) {
    return repository.hasTenant(tenantId);
  }

  @Scheduled(fixedRate = PING_PONG_RATE_MS)
  public void sendPingToAllSessions() {
    var now = Instant.now();

    var sessionsQueue =
        getAllSessions().entrySet().stream()
            .filter(entry -> entry.getValue().isOpen())
            .collect(Collectors.toCollection(LinkedList::new));

    while (!sessionsQueue.isEmpty()) {
      var entry = sessionsQueue.poll();
      var session = entry.getValue();
      var tenantId = entry.getKey();

      var lastPong = pongMap.getOrDefault(tenantId, Instant.MIN);
      if (now.minusMillis(PONG_TIMEOUT_MS).isAfter(lastPong)) {
        log.warn("No PONG from tenant {} in {}ms.", tenantId, PONG_TIMEOUT_MS);
        try {
          session.close();
        } catch (IOException e) {
          log.error("Error closing session for tenant {}", tenantId, e);
        }
        unregister(tenantId);
        continue;
      }

      try {
        var pingPayload = ByteBuffer.wrap(("ping:" + Instant.now()).getBytes());
        session.sendMessage(new PingMessage(pingPayload));
        log.info("PING message was sent for tenantId {}", entry.getKey());
      } catch (IOException e) {
        log.error("Error sending PING message to WebSocket for tenantId {}", entry.getKey(), e);
      }
    }
  }

  private String buildMessage(final UUID tenantId, final UUID messageId, final String content) {
    var json = ObjectMapperHolder.createObjectNode();
    json.put("type", "message");
    json.put("tenantId", tenantId.toString());
    json.put("messageId", messageId.toString());
    json.put("content", content);
    return json.toString();
  }
}

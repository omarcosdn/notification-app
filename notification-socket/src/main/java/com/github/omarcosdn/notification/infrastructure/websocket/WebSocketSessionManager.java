package com.github.omarcosdn.notification.infrastructure.websocket;

import com.github.omarcosdn.notification.core.repositories.TenantConnectionRepository;
import com.github.omarcosdn.notification.infrastructure.lock.DistributedLockManager;
import com.github.omarcosdn.notification.shared.utils.ObjectMapperHolder;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class WebSocketSessionManager {

  private static final long SESSION_HEALTH_CHECK_RATE_MS = 30000;
  private static final long PING_PONG_RATE_MS = 30000;
  private static final long PONG_TIMEOUT_MS = 60000;

  private final Map<UUID, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
  private final Map<UUID, Instant> pongMap = new ConcurrentHashMap<>();

  private final TenantConnectionRepository repository;
  private final DistributedLockManager lockManager;

  public WebSocketSessionManager(final TenantConnectionRepository repository,
                                 final DistributedLockManager lockManager) {
    this.repository = Objects.requireNonNull(repository);
    this.lockManager = Objects.requireNonNull(lockManager);
  }

  public boolean register(final UUID tenantId, final WebSocketSession session) {
    if (!lockManager.acquireLock(tenantId)) {
      log.warn("Could not acquire lock for tenant {}", tenantId);
      return false;
    }

    try {
      if (repository.hasTenant(tenantId)) {
        log.warn("Tenant {} already has an active session", tenantId);
        return false;
      }

      var existing = sessionMap.putIfAbsent(tenantId, session);
      if (existing != null) {
        log.error("Session already exists in memory for tenant {}", tenantId);
        return false;
      }

      repository.register(tenantId);
      pongMap.put(tenantId, Instant.now());
      log.info("Connection established for tenantId: {}", tenantId);
      return true;
    } catch (Exception e) {
      sessionMap.remove(tenantId);
      log.error("Error registering session for tenant {}", tenantId, e);
      return false;
    } finally {
      lockManager.releaseLock(tenantId);
    }
  }

  public void unregister(final UUID tenantId, final String sessionId) {
    if (!lockManager.acquireLock(tenantId)) {
      log.warn("Could not acquire lock for tenant {} during unregister", tenantId);
      return;
    }

    try {
      var currentSession = sessionMap.get(tenantId);

      if (currentSession == null || !currentSession.getId().equals(sessionId)) {
        log.info(
            "Ignoring unregister for tenant {} - session {} is not the current active session",
            tenantId,
            sessionId);
        return;
      }

      repository.unregister(tenantId);
      sessionMap.remove(tenantId);
      pongMap.remove(tenantId);
      log.info("Session {} closed for tenant {}", sessionId, tenantId);
    } catch (Exception e) {
      log.error("Error unregistering tenant {} session {}", tenantId, sessionId, e);
    } finally {
      lockManager.releaseLock(tenantId);
    }
  }

  public void updatePong(final UUID tenantId) {
    if (!lockManager.acquireLock(tenantId)) {
      log.warn("Could not acquire lock for tenant {} during pong update", tenantId);
      return;
    }

    try {
      log.debug("PONG received from tenant {}", tenantId);
      repository.refresh(tenantId);
      pongMap.put(tenantId, Instant.now());
    } catch (Exception e) {
      log.error("Error updating pong for tenant {}", tenantId, e);
    } finally {
      lockManager.releaseLock(tenantId);
    }
  }

  public void sendMessage(final UUID tenantId, final UUID messageId, final String content) {
    Optional.ofNullable(sessionMap.get(tenantId))
        .filter(WebSocketSession::isOpen)
        .ifPresent(
            session -> {
              try {
                var message = WebSocketMessage.build(tenantId, messageId, content);
                var json = ObjectMapperHolder.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
              } catch (IOException e) {
                log.error("Error sending message to WebSocket for tenant {}", tenantId, e);
              }
            });
  }

  public boolean hasSession(final UUID tenantId) {
    return repository.hasTenant(tenantId) && sessionMap.get(tenantId) != null;
  }

  @Scheduled(fixedRate = SESSION_HEALTH_CHECK_RATE_MS)
  public void checkSessionHealth() {
    sessionMap.forEach(
        (tenantId, session) -> {
          if (!session.isOpen()) {
            unregister(tenantId, session.getId());
          }
        });
  }

  @Scheduled(fixedRate = PING_PONG_RATE_MS)
  public void sendPingToAllSessions() {
    var now = Instant.now();
    sessionMap.forEach((tenantId, session) -> handleSessionPing(tenantId, session, now));
  }

  private void handleSessionPing(final UUID tenantId, final WebSocketSession session, final Instant now) {
    if (isSessionTimedOut(tenantId, now)) {
      closeTimedOutSession(tenantId, session);
      return;
    }

    sendPingMessage(tenantId, session);
  }

  private boolean isSessionTimedOut(final UUID tenantId, final Instant now) {
    var lastPong = pongMap.getOrDefault(tenantId, Instant.MIN);
    return now.minusMillis(PONG_TIMEOUT_MS).isAfter(lastPong);
  }

  private void closeTimedOutSession(final UUID tenantId, final WebSocketSession session) {
    log.warn("No PONG from tenant {} in {}ms.", tenantId, PONG_TIMEOUT_MS);
    try {
      if (session.isOpen()) {
        session.close();
      }
    } catch (IOException e) {
      log.error("Error closing session for tenant {}", tenantId, e);
    } finally {
      unregister(tenantId, session.getId());
    }
  }

  private void sendPingMessage(final UUID tenantId, final WebSocketSession session) {
    try {
      var pingPayload = ByteBuffer.wrap(("ping:" + Instant.now()).getBytes());
      session.sendMessage(new PingMessage(pingPayload));
      log.debug("PING message was sent for tenant {}", tenantId);
    } catch (IOException e) {
      log.error("Error sending PING message to WebSocket for tenant {}", tenantId, e);
    }
  }
}

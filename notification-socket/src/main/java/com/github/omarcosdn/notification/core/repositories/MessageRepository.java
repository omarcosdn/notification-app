package com.github.omarcosdn.notification.core.repositories;

import com.github.omarcosdn.notification.core.entities.Message;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {
  void upsert(Message entity);

  Optional<Message> findById(UUID messageId);

  List<Message> findUnAckedByTenantId(UUID tenantId);

  void markMessageAsAcked(UUID messageId);
}

package com.github.omarcosdn.notification.infrastructure.adapters.repositories;

import com.github.omarcosdn.notification.core.entities.Message;
import com.github.omarcosdn.notification.core.repositories.MessageRepository;
import com.github.omarcosdn.notification.infrastructure.adapters.jdbc.DatabaseClient;
import com.github.omarcosdn.notification.infrastructure.adapters.jdbc.RowMap;
import java.sql.Timestamp;
import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class MessageRepositoryDatabaseAdapter implements MessageRepository {

  private final DatabaseClient database;

  public MessageRepositoryDatabaseAdapter(final DatabaseClient database) {
    this.database = Objects.requireNonNull(database);
  }

  @Override
  public void upsert(final Message entity) {
    upsertEntity(entity);
  }

  @Override
  public Optional<Message> findById(final UUID messageId) {
    var sql =
            """
                SELECT message_id, tenant_id, content, received_at, acked, acked_at FROM MESSAGES m
                WHERE m.message_id = :messageId
            """;

    return this.database.queryOne(sql, Map.of("messageId", messageId), entityMapper());
  }

  @Override
  public List<Message> findUnAckedByTenantId(final UUID tenantId) {
    var sql =
        """
            SELECT message_id, tenant_id, content, received_at, acked, acked_at FROM MESSAGES m
            WHERE m.tenant_id = :tenantId AND acked = false
        """;

    return this.database.queryAll(sql, Map.of("tenantId", tenantId), entityMapper());
  }

  @Override
  public void markMessageAsAcked(final UUID messageId) {
    var sql =
        """
            UPDATE MESSAGES SET acked = true, acked_at = now()
            WHERE message_id = :messageId
        """;

    this.database.insert(sql, Map.of("messageId", messageId));
  }

  private void upsertEntity(final Message entity) {
    var sql =
        """
            INSERT INTO MESSAGES (message_id, tenant_id, content, received_at, acked, acked_at)
            VALUES (:messageId, :tenantId, :content::jsonb, :receivedAt, :acked, :ackedAt)
            ON CONFLICT (message_id)
            DO UPDATE SET content = EXCLUDED.content, received_at = EXCLUDED.received_at, acked = EXCLUDED.acked, acked_at = EXCLUDED.acked_at
        """;

    this.database.insert(sql, createParams(entity));
  }

  private Map<String, Object> createParams(final Message entity) {
    var params = new HashMap<String, Object>();
    params.put("messageId", entity.identity());
    params.put("tenantId", entity.getTenantId());
    params.put("content", entity.getContent());
    params.put("receivedAt", Timestamp.from(entity.getReceivedAt()));
    params.put("acked", entity.getAcked());
    params.put("ackedAt", entity.getAckedAt() != null ? Timestamp.from(entity.getAckedAt()) : null);

    return params;
  }

  private RowMap<Message> entityMapper() {
    return (rs) -> {
      var messageId = UUID.fromString(rs.getString("message_id"));
      var tenantId = UUID.fromString(rs.getString("tenant_id"));
      var content = rs.getString("content");
      var receivedAt = rs.getTimestamp("received_at").toInstant();
      var acked = rs.getBoolean("acked");
      var ackedAt = rs.getTimestamp("acked_at") != null ? rs.getTimestamp("acked_at").toInstant() : null;

      return Message.restore(messageId, tenantId, content, receivedAt, acked, ackedAt);
    };
  }
}

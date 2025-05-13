package com.github.omarcosdn.notification.core.entities;

import com.github.omarcosdn.notification.shared.entities.AggregateRoot;

import java.time.Instant;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class Message extends AggregateRoot {
    private final UUID tenantId;
    private final String content;
    private final Instant receivedAt;
    private final Boolean acked;
    private final Instant ackedAt;

    private Message(final UUID messageId,
                    final UUID tenantId,
                    final String content,
                    final Instant receivedAt,
                    final Boolean acked,
                    final Instant ackedAt) {
        super(messageId);
        this.tenantId = tenantId;
        this.content = content;
        this.receivedAt = receivedAt;
        this.acked = acked;
        this.ackedAt = ackedAt;
    }

    public static Message create(final UUID tenantId,
                                 final String content) {
        var messageId = UUID.randomUUID();
        var receivedAt = Instant.now();
        return new Message(messageId, tenantId, content, receivedAt, false, null);
    }

    public static Message restore(final UUID messageId,
                                  final UUID tenantId,
                                  final String content,
                                  final Instant receivedAt,
                                  final Boolean acked,
                                  final Instant ackedAt) {
        return new Message(messageId, tenantId, content, receivedAt, acked, ackedAt);
    }
}

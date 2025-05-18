package com.github.omarcosdn.notification.core.notification.v1;

import static com.github.omarcosdn.notification.shared.utils.Constants.NOTIFICATION_V1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationMessage {

  @JsonProperty("version")
  private String version;

  @JsonProperty("tenant_id")
  private UUID tenantId;

  @JsonProperty("idempotency_key")
  private UUID idempotencyKey;

  @JsonProperty("content")
  private String content;

  public static NotificationMessage buildV1(final UUID tenantId, final String content) {
    var idempotencyKey = UUID.randomUUID();

    return new NotificationMessage(NOTIFICATION_V1, tenantId, idempotencyKey, content);
  }
}

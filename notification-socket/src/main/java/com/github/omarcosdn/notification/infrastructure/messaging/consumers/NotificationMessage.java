package com.github.omarcosdn.notification.infrastructure.messaging.consumers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;

@Data
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
}

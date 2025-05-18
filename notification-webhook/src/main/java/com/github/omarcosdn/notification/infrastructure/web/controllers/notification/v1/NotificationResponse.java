package com.github.omarcosdn.notification.infrastructure.web.controllers.notification.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Getter;

@Getter
public class NotificationResponse {

  @JsonProperty("idempotency_key")
  private UUID idempotencyKey;

  public NotificationResponse(final UUID idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
  }
}

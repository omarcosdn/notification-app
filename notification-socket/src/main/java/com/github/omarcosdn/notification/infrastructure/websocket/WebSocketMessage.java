package com.github.omarcosdn.notification.infrastructure.websocket;

import static com.github.omarcosdn.notification.shared.utils.Constants.NOTIFICATION_TYPE;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebSocketMessage {

  @JsonProperty("type")
  private String type;

  @JsonProperty("tenant_id")
  private UUID tenantId;

  @JsonProperty("message_id")
  private UUID messageId;

  @JsonProperty("content")
  private String content;

  public static WebSocketMessage build(final UUID tenantId, final UUID messageId, final String content) {
    return new WebSocketMessage(NOTIFICATION_TYPE, tenantId, messageId, content);
  }
}

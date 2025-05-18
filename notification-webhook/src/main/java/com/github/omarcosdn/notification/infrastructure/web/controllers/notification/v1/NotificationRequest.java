package com.github.omarcosdn.notification.infrastructure.web.controllers.notification.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationRequest {

  @JsonProperty("tenant_id")
  private UUID tenantId;

  @JsonProperty("content")
  private String content;
}

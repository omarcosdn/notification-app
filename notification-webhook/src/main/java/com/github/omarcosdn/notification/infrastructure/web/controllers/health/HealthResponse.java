package com.github.omarcosdn.notification.infrastructure.web.controllers.health;

public record HealthResponse(int status, String message) {

  public static HealthResponse build() {
    return new HealthResponse(200, "everything is fine");
  }
}

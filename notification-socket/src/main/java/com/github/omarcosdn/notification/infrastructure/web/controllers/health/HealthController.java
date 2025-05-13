package com.github.omarcosdn.notification.infrastructure.web.controllers.health;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "health")
public class HealthController {

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HealthResponse> isHealthy() {
    return ResponseEntity.ok(HealthResponse.build());
  }
}

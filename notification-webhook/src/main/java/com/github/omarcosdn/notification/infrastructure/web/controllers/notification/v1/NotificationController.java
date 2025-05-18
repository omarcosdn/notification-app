package com.github.omarcosdn.notification.infrastructure.web.controllers.notification.v1;

import com.github.omarcosdn.notification.core.NotificationProducer;
import com.github.omarcosdn.notification.core.notification.v1.NotificationMessage;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "v1/message")
public class NotificationController {

  private final NotificationProducer producer;

  public NotificationController(final NotificationProducer producer) {
    this.producer = Objects.requireNonNull(producer);
  }

  @PostMapping(
      value = "send",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<NotificationResponse> sent(@RequestBody @Valid NotificationRequest request) {
    var notificationMessage = NotificationMessage.buildV1(request.getTenantId(), request.getContent());

    producer.sendNotification(notificationMessage);

    return ResponseEntity.ok(new NotificationResponse(notificationMessage.getIdempotencyKey()));
  }
}

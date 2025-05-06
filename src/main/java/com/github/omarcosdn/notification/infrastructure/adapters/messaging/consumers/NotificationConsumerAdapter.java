package com.github.omarcosdn.notification.infrastructure.adapters.messaging.consumers;

import com.github.omarcosdn.notification.core.usecases.CreateMessageUseCase;
import com.github.omarcosdn.notification.infrastructure.config.RabbitConfig;
import com.github.omarcosdn.notification.shared.utils.ObjectMapperHolder;
import java.util.Objects;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumerAdapter {

  private final CreateMessageUseCase useCase;

  public NotificationConsumerAdapter(final CreateMessageUseCase useCase) {
    this.useCase = Objects.requireNonNull(useCase);
  }

  @RabbitListener(queues = RabbitConfig.NOTIFICATION_V1_QUEUE)
  public void onMessage(final String message) {
    var json = ObjectMapperHolder.readTree(message);

    var tenantId = UUID.fromString(json.get("tenantId").asText());
    var content = json.get("content").asText();

    useCase.execute(new MessageDto(tenantId, content));
  }

  record MessageDto(UUID tenantId, String content) implements CreateMessageUseCase.Input {}
}

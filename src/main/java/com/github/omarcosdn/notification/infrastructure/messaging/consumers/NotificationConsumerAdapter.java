package com.github.omarcosdn.notification.infrastructure.messaging.consumers;

import com.github.omarcosdn.notification.core.services.MessageDispatcher;
import com.github.omarcosdn.notification.infrastructure.config.RabbitConfig;
import com.github.omarcosdn.notification.shared.utils.ObjectMapperHolder;
import java.util.Objects;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumerAdapter {

  private final MessageDispatcher dispatcher;

  public NotificationConsumerAdapter(final MessageDispatcher dispatcher) {
    this.dispatcher = Objects.requireNonNull(dispatcher);
  }

  @RabbitListener(queues = RabbitConfig.NOTIFICATION_V1_QUEUE)
  public void onMessage(final String message) {
    var json = ObjectMapperHolder.readTree(message);

    // TODO: must validate message payload
    var tenantId = UUID.fromString(json.get("tenantId").asText());
    var content = json.get("content").asText();

    dispatcher.dispatch(tenantId, content);
  }
}

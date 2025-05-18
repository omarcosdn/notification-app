package com.github.omarcosdn.notification.infrastructure.messaging.consumers;

import com.github.omarcosdn.notification.core.services.MessageDispatcher;
import com.github.omarcosdn.notification.infrastructure.config.AmqpConfig;
import com.github.omarcosdn.notification.shared.exceptions.UnsupportedMessageVersionException;
import com.github.omarcosdn.notification.shared.utils.ObjectMapperHolder;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumerAdapter {

  private static final Set<String> SUPPORTED_VERSIONS = Set.of("v1");

  private final MessageDispatcher dispatcher;

  public NotificationConsumerAdapter(final MessageDispatcher dispatcher) {
    this.dispatcher = Objects.requireNonNull(dispatcher);
  }

  @RabbitListener(queues = AmqpConfig.NOTIFICATION_V1_QUEUE)
  public void onMessage(final String message, final Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
    try {
      var notificationMessage = ObjectMapperHolder.readValue(message, NotificationMessage.class);
      validateMessageVersion(notificationMessage.getVersion());

      dispatcher.dispatch(notificationMessage.getTenantId(),
                          notificationMessage.getIdempotencyKey(),
                          notificationMessage.getContent());
      channel.basicAck(tag, false);
    } catch (Exception e) {
      channel.basicReject(tag, false);
      log.error("An error occurred while processing message.", e);
      throw e;
    }
  }

  private void validateMessageVersion(final String version) {
    if (version == null || !SUPPORTED_VERSIONS.contains(version.toLowerCase())) {
      throw new UnsupportedMessageVersionException(version);
    }
  }
}

package com.github.omarcosdn.notification.core;

import static com.github.omarcosdn.notification.infrastructure.config.AmqpConfig.NOTIFICATION_V1_EXCHANGE;

import com.github.omarcosdn.notification.core.notification.v1.NotificationMessage;
import com.github.omarcosdn.notification.infrastructure.config.AmqpConfig;
import com.github.omarcosdn.notification.shared.utils.ObjectMapperHolder;
import java.util.Objects;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

  private final RabbitTemplate rabbitTemplate;

  public NotificationProducer(final RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = Objects.requireNonNull(rabbitTemplate);
  }

  public void sendNotification(final NotificationMessage notificationMessage) {
    var payload = ObjectMapperHolder.writeValueAsString(notificationMessage);
    rabbitTemplate.convertAndSend(NOTIFICATION_V1_EXCHANGE, AmqpConfig.EMPTY_RK, payload);
  }
}

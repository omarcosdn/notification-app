package com.github.omarcosdn.notification.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class RabbitConfig {

  public static final String NOTIFICATION_EXCHANGE =
      "com.github.omarcosdn.exchange.notification.pubsub";
  public static final String NOTIFICATION_V1_QUEUE = "com.github.omarcosdn.queue.notification.v1";

  @Bean
  public FanoutExchange notificationExchange() {
    return new FanoutExchange(NOTIFICATION_EXCHANGE);
  }

  @Bean
  public Queue notificationV1Queue() {
    return new Queue(NOTIFICATION_V1_QUEUE);
  }

  @Bean
  public Binding participantVerificationV1QueueBinding() {
    return BindingBuilder.bind(notificationV1Queue()).to(notificationExchange());
  }
}

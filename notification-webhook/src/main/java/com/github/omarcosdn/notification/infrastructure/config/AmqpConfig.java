package com.github.omarcosdn.notification.infrastructure.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class AmqpConfig {

  public static final String NOTIFICATION_EXCHANGE = "com.github.omarcosdn.exchange.notification.v1.pubsub";
  public static final String EMPTY_RK = "";

  @Bean
  public FanoutExchange notificationExchange() {
    return new FanoutExchange(NOTIFICATION_EXCHANGE);
  }
}

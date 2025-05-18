package com.github.omarcosdn.notification.infrastructure.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class AmqpConfig {

  public static final String NOTIFICATION_V1_EXCHANGE = "notification.v1.exchange";

  public static final String EMPTY_RK = "";

  @Bean
  public FanoutExchange notificationExchangeV1() {
    return new FanoutExchange(NOTIFICATION_V1_EXCHANGE);
  }
}

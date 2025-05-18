package com.github.omarcosdn.notification.infrastructure.config;

import java.util.HashMap;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class AmqpConfig {

  public static final String NOTIFICATION_V1_EXCHANGE = "notification.v1.exchange";
  public static final String NOTIFICATION_V1_DLQ_EXCHANGE = "notification.v1.dlq.exchange";

  public static final String NOTIFICATION_V1_QUEUE = "notification.v1.queue";
  public static final String NOTIFICATION_V1_DLQ_QUEUE = "notification.v1.dlq.queue";

  @Bean
  public FanoutExchange notificationExchangeV1() {
    return new FanoutExchange(NOTIFICATION_V1_EXCHANGE);
  }

  @Bean
  public FanoutExchange deadLetterExchangeV1() {
    return new FanoutExchange(NOTIFICATION_V1_DLQ_EXCHANGE);
  }

  @Bean
  public Queue notificationQueueV1() {
    var args = new HashMap<String, Object>();
    args.put("x-dead-letter-exchange", NOTIFICATION_V1_DLQ_EXCHANGE);
    return new Queue(NOTIFICATION_V1_QUEUE, true, false, false, args);
  }

  @Bean
  public Binding notificationQueueBindingV1() {
    return BindingBuilder.bind(notificationQueueV1()).to(notificationExchangeV1());
  }

  @Bean
  public Queue notificationDeadLetterQueueV1() {
    return new Queue(NOTIFICATION_V1_DLQ_QUEUE);
  }

  @Bean
  public Binding notificationDeadLetterQueueBindingV1() {
    return BindingBuilder.bind(notificationDeadLetterQueueV1()).to(deadLetterExchangeV1());
  }
}

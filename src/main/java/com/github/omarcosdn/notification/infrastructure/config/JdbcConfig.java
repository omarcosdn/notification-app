package com.github.omarcosdn.notification.infrastructure.config;

import com.github.omarcosdn.notification.infrastructure.adapters.jdbc.DatabaseClient;
import com.github.omarcosdn.notification.infrastructure.adapters.jdbc.impl.JdbcClientAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration(proxyBeanMethods = false)
public class JdbcConfig {

  @Bean
  DatabaseClient databaseClient(final JdbcClient jdbcClient) {
    return new JdbcClientAdapter(jdbcClient);
  }
}

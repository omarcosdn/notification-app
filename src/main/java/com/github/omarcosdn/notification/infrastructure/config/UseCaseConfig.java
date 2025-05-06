package com.github.omarcosdn.notification.infrastructure.config;

import com.github.omarcosdn.notification.core.repositories.MessageRepository;
import com.github.omarcosdn.notification.core.usecases.CreateMessageUseCase;
import com.github.omarcosdn.notification.core.usecases.interactors.CreateMessageInteractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

  @Bean
  CreateMessageUseCase createMessageUseCase(final MessageRepository repository) {
    return new CreateMessageInteractor(repository);
  }
}

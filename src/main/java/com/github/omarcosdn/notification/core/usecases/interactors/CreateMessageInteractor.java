package com.github.omarcosdn.notification.core.usecases.interactors;

import com.github.omarcosdn.notification.core.entities.Message;
import com.github.omarcosdn.notification.core.repositories.MessageRepository;
import com.github.omarcosdn.notification.core.usecases.CreateMessageUseCase;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateMessageInteractor implements CreateMessageUseCase {

  private final MessageRepository repository;

  public CreateMessageInteractor(final MessageRepository repository) {
    this.repository = Objects.requireNonNull(repository);
  }

  @Override
  public Output execute(final Input input) {
    var message = Message.create(input.tenantId(), input.content());

    repository.upsert(message);

    return new StdOutput(message.identity());
  }

  record StdOutput(UUID messageId) implements Output {}
}

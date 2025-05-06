package com.github.omarcosdn.notification.core.usecases;

import static com.github.omarcosdn.notification.core.usecases.CreateMessageUseCase.*;

import com.github.omarcosdn.notification.shared.usecases.ExecutableUseCase;
import java.util.UUID;

public interface CreateMessageUseCase extends ExecutableUseCase<Input, Output> {
  interface Input {
    UUID tenantId();

    String content();
  }

  interface Output {
    UUID messageId();
  }
}

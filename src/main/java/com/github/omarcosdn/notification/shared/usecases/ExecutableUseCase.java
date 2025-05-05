package com.github.omarcosdn.notification.shared.usecases;

public interface ExecutableUseCase<I, O> {
  O execute(I input);
}

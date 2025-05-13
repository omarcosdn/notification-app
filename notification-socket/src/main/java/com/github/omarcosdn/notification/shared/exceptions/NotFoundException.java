package com.github.omarcosdn.notification.shared.exceptions;

public class NotFoundException extends DomainException {
  public NotFoundException(final String message) {
    super(message);
  }

  public static NotFoundException with(final String message) {
    return new NotFoundException(message);
  }
}

package com.github.omarcosdn.notification.shared.exceptions;

public class UnsupportedMessageVersionException extends DomainException {
  public UnsupportedMessageVersionException(final String version) {
    super("Unsupported Message Version %s".formatted(version));
  }

  public static UnsupportedMessageVersionException with(final String version) {
    return new UnsupportedMessageVersionException(version);
  }
}

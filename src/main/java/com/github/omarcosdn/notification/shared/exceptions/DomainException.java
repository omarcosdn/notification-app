package com.github.omarcosdn.notification.shared.exceptions;

/**
 * Represents a domain-specific exception in the domain-driven design context. This exception is
 * used to indicate business rule violations or other domain-specific errors.
 */
public class DomainException extends RuntimeException {
  public DomainException(final String message) {
    super(message);
  }

  public static DomainException with(final String message) {
    return new DomainException(message);
  }
}

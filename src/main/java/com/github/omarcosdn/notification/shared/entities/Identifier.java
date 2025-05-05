package com.github.omarcosdn.notification.shared.entities;

/**
 * Represents a generic identifier in the domain-driven design context. An identifier is a value
 * object that uniquely identifies an entity or aggregate root.
 *
 * @param <T> The type of the value of the identifier.
 */
public interface Identifier<T> {

  /**
   * Returns the value of the identifier.
   *
   * @return The value of the identifier.
   */
  T value();
}

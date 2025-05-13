package com.github.omarcosdn.notification.shared.entities;

import java.util.UUID;

/**
 * Represents the root of an aggregate in the domain-driven design context. An aggregate root is a
 * special type of entity that serves as the entry point for an aggregate.
 */
public abstract class AggregateRoot extends Entity {

  /**
   * Constructs a new aggregate root with the specified identity.
   *
   * @param identity The unique identifier of the aggregate root. Must not be null.
   */
  protected AggregateRoot(final UUID identity) {
    super(identity);
  }
}

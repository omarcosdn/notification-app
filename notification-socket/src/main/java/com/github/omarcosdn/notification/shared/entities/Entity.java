package com.github.omarcosdn.notification.shared.entities;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Represents a generic entity in the domain-driven design context. An entity is characterized by
 * having a unique identity.
 */
@EqualsAndHashCode(callSuper = false)
@Getter
public abstract class Entity {

  @Accessors(fluent = true)
  protected final UUID identity;

  /**
   * Constructs a new entity with the specified identity.
   *
   * @param identity The unique identifier of the entity. Must not be null.
   */
  protected Entity(final UUID identity) {
    this.identity = identity;
  }
}

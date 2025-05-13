package com.github.omarcosdn.notification.shared.utils;

import com.github.omarcosdn.notification.shared.exceptions.DomainException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for performing common assertions in the domain layer. Provides methods to assert
 * conditions and throw domain-specific exceptions when assertions fail.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AssertionUtils {
  public static <T> T assertNotNull(T target, String message) {
    if (target == null) {
      throw DomainException.with(message);
    }
    return target;
  }

  public static String assertNotEmpty(String target, String message) {
    if (target == null || target.isBlank()) {
      throw DomainException.with(message);
    }
    return target;
  }

  public static void assertTrue(Boolean target, String message) {
    if (Boolean.FALSE.equals(target)) {
      throw DomainException.with(message);
    }
  }
}

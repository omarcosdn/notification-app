package com.github.omarcosdn.notification.shared.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.Callable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectMapperHolder {
  private static final ObjectMapper MAPPER =
      new Jackson2ObjectMapperBuilder()
          .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
          .build();

  public static String writeValueAsString(final Object object) {
    return invoke(() -> MAPPER.writeValueAsString(object));
  }

  public static <T> T readValue(final String json, final Class<T> clazz) {
    return invoke(() -> MAPPER.readValue(json, clazz));
  }

  public static JsonNode readTree(final String json) {
    return invoke(() -> MAPPER.readTree(json));
  }

  public static ObjectNode createObjectNode() {
    return invoke(MAPPER::createObjectNode);
  }

  private static <T> T invoke(final Callable<T> callable) {
    try {
      return callable.call();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}

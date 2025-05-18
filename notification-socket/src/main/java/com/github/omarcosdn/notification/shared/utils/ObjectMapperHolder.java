package com.github.omarcosdn.notification.shared.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.Callable;
import lombok.experimental.UtilityClass;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@UtilityClass
public class ObjectMapperHolder {
  private final ObjectMapper MAPPER =
      new Jackson2ObjectMapperBuilder()
          .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
          .build();

  public String writeValueAsString(final Object object) {
    return invoke(() -> MAPPER.writeValueAsString(object));
  }

  public <T> T readValue(final String json, final Class<T> clazz) {
    return invoke(() -> MAPPER.readValue(json, clazz));
  }

  public JsonNode readTree(final String json) {
    return invoke(() -> MAPPER.readTree(json));
  }

  public ObjectNode createObjectNode() {
    return invoke(MAPPER::createObjectNode);
  }

  private <T> T invoke(final Callable<T> callable) {
    try {
      return callable.call();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}

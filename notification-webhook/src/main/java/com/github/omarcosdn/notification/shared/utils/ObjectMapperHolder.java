package com.github.omarcosdn.notification.shared.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
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

  private <T> T invoke(final Callable<T> callable) {
    try {
      return callable.call();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}

package com.github.omarcosdn.notification.infrastructure.adapters.jdbc;

import java.util.Map;
import java.util.Optional;

public interface DatabaseClient {

  void insert(String sql, Map<String, Object> params);

  <T> Optional<T> queryOne(String sql, Map<String, Object> params, RowMap<T> mapper);
}

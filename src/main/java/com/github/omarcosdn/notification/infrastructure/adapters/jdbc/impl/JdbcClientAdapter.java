package com.github.omarcosdn.notification.infrastructure.adapters.jdbc.impl;

import com.github.omarcosdn.notification.infrastructure.adapters.jdbc.DatabaseClient;
import com.github.omarcosdn.notification.infrastructure.adapters.jdbc.RowMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JdbcClientAdapter implements DatabaseClient {

  private final JdbcClient target;

  public JdbcClientAdapter(final JdbcClient target) {
    this.target = Objects.requireNonNull(target);
  }

  @Override
  public void insert(final String sql, final Map<String, Object> params) {
    this.target.sql(sql).params(params).update();
  }

  @Override
  public <T> Optional<T> queryOne(String sql, Map<String, Object> params, RowMap<T> mapper) {
    return this.target.sql(sql).params(params).query(new RowMapAdapter<>(mapper)).optional();
  }

  @Override
  public <T> List<T> queryAll(String sql, Map<String, Object> params, RowMap<T> mapper) {
    return this.target.sql(sql).params(params).query(new RowMapAdapter<>(mapper)).list();
  }
}

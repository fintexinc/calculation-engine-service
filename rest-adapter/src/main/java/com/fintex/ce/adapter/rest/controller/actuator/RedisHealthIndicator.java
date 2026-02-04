package com.fintex.ce.adapter.rest.controller.actuator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

  public static final String PONG = "pong";
  private final RedisConnectionFactory redisConnectionFactory;

  @Override
  public Health health() {
    if (isHealthy()) {
      return Health.up().build();
    }
    return Health.down().build();
  }

  private boolean isHealthy() {
    RedisConnection connection = RedisConnectionUtils.getConnection(this.redisConnectionFactory);
    try {
      String pong = connection.ping();
      return PONG.equalsIgnoreCase(pong);
    } finally {
      RedisConnectionUtils.releaseConnection(connection, this.redisConnectionFactory);
    }
  }
}
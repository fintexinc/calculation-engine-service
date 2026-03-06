package com.fintex.ce.adapter.rest.controller.actuator;

import com.fintex.ce.port.output.cache.CacheHealthPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

  private final CacheHealthPort cacheHealthPort;

  @Override
  public Health health() {
    if (cacheHealthPort.isHealthy()) {
      return Health.up().build();
    }
    return Health.down().build();
  }
}
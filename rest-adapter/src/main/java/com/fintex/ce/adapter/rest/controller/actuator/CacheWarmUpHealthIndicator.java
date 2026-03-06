package com.fintex.ce.adapter.rest.controller.actuator;

import com.fintex.ce.port.output.cache.CacheWarmUpPort;
import lombok.AllArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CacheWarmUpHealthIndicator implements HealthIndicator {

  private final CacheWarmUpPort cacheWarmUpPort;

  @Override
  public Health health() {
    final CacheWarmUpPort.SchedulerRunInfo schedulerRunInfo = cacheWarmUpPort
        .cacheWarmUpSchedulerRunCheck();
    if (!schedulerRunInfo.runInLast24Hours()) {
      return Health.down()
          .withDetail("info", "cache warm-up scheduler didn't run in past 24 hours")
          .build();
    }
    return Health.up()
        .withDetail("info", ("last time cache warm-up scheduler run: " + schedulerRunInfo.lastTimeRun()))
        .build();
  }

}
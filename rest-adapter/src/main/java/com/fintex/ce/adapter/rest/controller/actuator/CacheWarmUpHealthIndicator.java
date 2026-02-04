package com.fintex.ce.adapter.rest.controller.actuator;

import com.fintex.ce.adapter.cache.statistic.CacheWarmUpServiceImpl;
import com.fintex.ce.adapter.cache.statistic.CacheWarmUpService;
import lombok.AllArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CacheWarmUpHealthIndicator implements HealthIndicator {

  private final CacheWarmUpService cacheWarmUpService;

  @Override
  public Health health() {
    final CacheWarmUpServiceImpl.SchedulerRunInfoDto schedulerRunInfoDto = cacheWarmUpService
        .cacheWarmUpSchedulerRunCheck();
    if (!schedulerRunInfoDto.isRunInLast24Hours()) {
      return Health.down()
          .withDetail("info", "cache warm-up scheduler didn't run in past 24 hours")
          .build();
    }
    return Health.up()
        .withDetail("info", ("last time cache warm-up scheduler run: " + schedulerRunInfoDto.getLastTimeRun()))
        .build();
  }

}

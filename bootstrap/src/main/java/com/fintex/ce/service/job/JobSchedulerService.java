package com.fintex.ce.service.job;

import com.fintex.ce.port.output.cache.CacheCleanupPort;
import com.fintex.ce.port.output.cache.CacheWarmUpPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobSchedulerService {
  public static final int TWENTY_SECONDS = 20000;

  private final CacheWarmUpPort cacheWarmUpPort;
  private final CacheCleanupPort cacheCleanupPort;

  public JobSchedulerService(final CacheWarmUpPort cacheWarmUpPort,
      final CacheCleanupPort cacheCleanupPort) {
    this.cacheWarmUpPort = cacheWarmUpPort;
    this.cacheCleanupPort = cacheCleanupPort;
  }

  /**
   * At 03:00:00am EST every day
   */
  @Scheduled(cron = "0 0 3 * * *", zone = "EST")
  public void clearCaffeineCache() {
    log.info("Start clearing Caffeine cached");
    cacheCleanupPort.evictLocalCaches();
    log.info("Stop clearing Caffeine cache");
  }

  /**
   * At 03:00:00am EST every day
   */
  @Scheduled(cron = "0 0 3 * * *", zone = "EST")
  public void warmUpRedisCache() {
    log.info("Start warming Redis cache");
    cacheCleanupPort.clearCache();
    sleepTwentySeconds();
    cacheWarmUpPort.run();
    log.info("Stop warming Redis cache");
  }

  private void sleepTwentySeconds() {
    try {
      Thread.sleep(TWENTY_SECONDS);
    } catch (InterruptedException e) {
      log.warn("cannot sleep");
      Thread.currentThread().interrupt();
    }
  }

}

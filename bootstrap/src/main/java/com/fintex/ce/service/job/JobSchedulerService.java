package com.fintex.ce.service.job;

import com.fintex.ce.adapter.cache.statistic.CacheWarmUpService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobSchedulerService {
  public static final int TWENTY_SECONDS = 20000;

  private final CacheWarmUpService cacheWarmUpService;
  private final CaffeineCacheManager caffeine1HourCacheManager;

  public JobSchedulerService(final CacheWarmUpService cacheWarmUpService,
      final CaffeineCacheManager caffeine1HourCacheManager) {
    this.cacheWarmUpService = cacheWarmUpService;
    this.caffeine1HourCacheManager = caffeine1HourCacheManager;
  }

  /**
   * At 03:00:00am EST every day
   */
  @Scheduled(cron = "0 0 3 * * *", zone = "EST")
  public void clearCaffeineCache() {
    log.info("Start clearing Caffeine cached");
    caffeine1HourCacheManager.getCacheNames()
        .forEach(cacheName -> caffeine1HourCacheManager.getCache(cacheName).invalidate());
    log.info("Stop clearing Caffeine cache");
  }

  /**
   * At 03:00:00am EST every day Keeps lock at least 1 minute and at most 1 hour. ShedLock releases the lock directly
   * after the task finishes if job takes more than 1 minute, otherwise releases lock after 1 minute.
   */
  @Scheduled(cron = "0 0 3 * * *", zone = "EST")
  @SchedulerLock(name = "warmUpRedisCache", lockAtLeastFor = "PT1M", lockAtMostFor = "PT1H")
  public void warmUpRedisCache() {
    log.info("Start warming Redis cache");
    cacheWarmUpService.clearCache();
    sleepTwentySeconds();
    cacheWarmUpService.run();
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
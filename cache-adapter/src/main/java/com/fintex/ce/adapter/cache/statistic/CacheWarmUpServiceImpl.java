package com.fintex.ce.adapter.cache.statistic;

import com.fintex.ce.adapter.cache.entity.RCacheWarmUpDate;
import com.fintex.ce.adapter.cache.repository.CacheWarmUpSchedulerDateRedisRepository;
import com.fintex.ce.port.output.cache.CacheCleanupPort;
import com.fintex.ce.port.output.cache.CacheWarmUpPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

@Service
@Slf4j
public class CacheWarmUpServiceImpl implements CacheWarmUpService {

  private final CacheWarmUpSchedulerDateRedisRepository cacheWarmUpSchedulerDateRedisRepository;
  private final CacheCleanupPort cacheCleanupPort;

  public CacheWarmUpServiceImpl(
      CacheWarmUpSchedulerDateRedisRepository cacheWarmUpSchedulerDateRedisRepository,
      CacheCleanupPort cacheCleanupPort) {
    this.cacheWarmUpSchedulerDateRedisRepository = cacheWarmUpSchedulerDateRedisRepository;
    this.cacheCleanupPort = cacheCleanupPort;
  }

  @Override
  public void run() {
    cacheCleanupPort.clearCache();
    cacheWarmUpSchedulerDateRedisRepository.save(new RCacheWarmUpDate().setZonedDateTime(ZonedDateTime.now()));
  }

  @Override
  public CacheWarmUpPort.SchedulerRunInfo cacheWarmUpSchedulerRunCheck() {
    final RCacheWarmUpDate rCacheWarmUpDate = cacheWarmUpSchedulerDateRedisRepository.findAllByPrefixEnv()
        .stream()
        .findFirst()
        .orElse(new RCacheWarmUpDate());

    final boolean wasRunInLast24Hours = lastRunOfCacheWarmUpWasLessThan24HoursAgo(rCacheWarmUpDate);
    return new CacheWarmUpPort.SchedulerRunInfo(wasRunInLast24Hours, rCacheWarmUpDate.getZonedDateTime());
  }

  private boolean lastRunOfCacheWarmUpWasLessThan24HoursAgo(final RCacheWarmUpDate rCacheWarmUpDate) {
    return Objects.nonNull(rCacheWarmUpDate.getZonedDateTime())
        && Duration.between(ZonedDateTime.now(), rCacheWarmUpDate.getZonedDateTime()).toHours() > -24;
  }

}

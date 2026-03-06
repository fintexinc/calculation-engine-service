package com.fintex.ce.port.output.cache;

import java.time.ZonedDateTime;

/**
 * Port interface for cache warm-up operations.
 * Implemented by the cache-adapter module.
 */
public interface CacheWarmUpPort {

  void run();

  SchedulerRunInfo cacheWarmUpSchedulerRunCheck();

  record SchedulerRunInfo(boolean runInLast24Hours, ZonedDateTime lastTimeRun) {
  }

}
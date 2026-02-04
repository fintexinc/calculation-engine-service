package com.fintex.ce.adapter.cache.statistic;

public interface CacheWarmUpService {

  void run();

  void clearCache();

  CacheWarmUpServiceImpl.SchedulerRunInfoDto cacheWarmUpSchedulerRunCheck();

}

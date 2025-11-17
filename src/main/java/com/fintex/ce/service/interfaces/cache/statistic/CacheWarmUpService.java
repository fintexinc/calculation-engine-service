package com.fintex.ce.service.interfaces.cache.statistic;

import com.fintex.ce.service.impl.cache.statistic.CacheWarmUpServiceImpl;

public interface CacheWarmUpService {

    void run();

    void clearCache();

    CacheWarmUpServiceImpl.SchedulerRunInfoDto cacheWarmUpSchedulerRunCheck();

}

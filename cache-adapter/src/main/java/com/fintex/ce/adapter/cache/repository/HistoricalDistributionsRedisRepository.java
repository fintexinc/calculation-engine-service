package com.fintex.ce.adapter.cache.repository;

import com.fintex.ce.adapter.cache.entity.RHistoricalDistributions;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.fintex.ce.adapter.cache.config.CacheConfig.HISTORICAL_DISTRIBUTIONS;
import static com.fintex.ce.adapter.cache.config.RedisConfig.PREFIX_ENV;

@Repository
public interface HistoricalDistributionsRedisRepository extends CoreRedisCacheRepository<RHistoricalDistributions> {

  @Override
  @Cacheable(value = HISTORICAL_DISTRIBUTIONS, cacheManager = "caffeine1HourCacheManager", unless = "#result == null or #result.size() == 0")
  default List<RHistoricalDistributions> findAllByHoldingId(final String holdingId) {
    return this.findAllByHoldingIdAndPrefixEnv(holdingId, PREFIX_ENV);
  }

}

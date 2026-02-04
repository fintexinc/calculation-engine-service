package com.fintex.ce.adapter.cache.repository.monthlyreturns;

import com.fintex.ce.adapter.cache.entity.RMonthlyReturns;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.fintex.ce.adapter.cache.config.CacheConfig.FIND_ALL_BY_HOLDING_ID;
import static com.fintex.ce.adapter.cache.config.RedisConfig.PREFIX_ENV;

@Repository
public interface MonthlyReturnsRepository extends CoreRedisCacheRepository<RMonthlyReturns> {

  @Override
  @Cacheable(value = FIND_ALL_BY_HOLDING_ID, cacheManager = "caffeine1HourCacheManager", unless = "#result == null or #result.size() == 0")
  default List<RMonthlyReturns> findAllByHoldingId(final String holdingId) {
    return this.findAllByHoldingIdAndPrefixEnv(holdingId, PREFIX_ENV);
  }

}

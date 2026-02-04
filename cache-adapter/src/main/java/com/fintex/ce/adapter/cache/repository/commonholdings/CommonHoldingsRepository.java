package com.fintex.ce.adapter.cache.repository.commonholdings;

import com.fintex.ce.adapter.cache.entity.topcommonholdings.RCommonHoldings;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

import static com.fintex.ce.adapter.cache.config.CacheConfig.TOP_COMMON_HOLDINGS;
import static com.fintex.ce.adapter.cache.config.RedisConfig.PREFIX_ENV;

public interface CommonHoldingsRepository extends CoreRedisCacheRepository<RCommonHoldings> {

  @Override
  @Cacheable(value = TOP_COMMON_HOLDINGS, cacheManager = "caffeine1HourCacheManager", unless = "#result == null or #result.size() == 0")
  default List<RCommonHoldings> findAllByHoldingId(final String holdingId) {
    return this.findAllByHoldingIdAndPrefixEnv(holdingId, PREFIX_ENV);
  }

}

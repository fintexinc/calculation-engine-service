package com.fintex.ce.adapter.cache.repository;

import com.fintex.ce.adapter.cache.entity.RHistoricalNavPrices;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.fintex.ce.adapter.cache.config.CacheConfig.HISTORICAL_NAV_PRICES;
import static com.fintex.ce.adapter.cache.config.RedisConfig.PREFIX_ENV;

@Repository
public interface HistoricalNavPricesRedisRepository extends CoreRedisCacheRepository<RHistoricalNavPrices> {

  @Override
  @Cacheable(value = HISTORICAL_NAV_PRICES, cacheManager = "caffeine1HourCacheManager", unless = "#result == null or #result.size() == 0")
  default List<RHistoricalNavPrices> findAllByHoldingId(final String holdingId) {
    return this.findAllByHoldingIdAndPrefixEnv(holdingId, PREFIX_ENV);
  }

}

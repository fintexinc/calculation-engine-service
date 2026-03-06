package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.entity.core.RedisId;
import com.fintex.ce.adapter.cache.repository.FxRatesRepository;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import com.fintex.ce.port.output.cache.CacheCleanupPort;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
public class CacheCleanupAdapter implements CacheCleanupPort {

  private final List<CoreRedisCacheRepository<?>> coreRedisCacheRepositories;
  private final FxRatesRepository fxRatesRepository;
  private final CacheManager redis5DaysCacheManager;
  private final CacheManager caffeine1HourCacheManager;
  private final RedisConnectionFactory redisConnectionFactory;
  private final CacheKeyPrefix cacheKeyPrefix;

  public CacheCleanupAdapter(final List<CoreRedisCacheRepository<?>> coreRedisCacheRepositories,
      final FxRatesRepository fxRatesRepository,
      @Qualifier("redis5DaysCacheManager") final CacheManager redis5DaysCacheManager,
      @Qualifier("caffeine1HourCacheManager") final CacheManager caffeine1HourCacheManager,
      final RedisConnectionFactory redisConnectionFactory,
      final CacheKeyPrefix cacheKeyPrefix) {
    this.coreRedisCacheRepositories = coreRedisCacheRepositories;
    this.fxRatesRepository = fxRatesRepository;
    this.redis5DaysCacheManager = redis5DaysCacheManager;
    this.caffeine1HourCacheManager = caffeine1HourCacheManager;
    this.redisConnectionFactory = redisConnectionFactory;
    this.cacheKeyPrefix = cacheKeyPrefix;
  }

  @Override
  public void removeFxRatesFromCache() {
    fxRatesRepository.deleteAll();
    log.info("Remove fx rates from redis cache");
  }

  @Override
  public void removeByHoldingId(final String holdingId) {
    coreRedisCacheRepositories.forEach(repository -> {
      final List<? extends RedisId> redisIds = repository.findAllByHoldingId(holdingId);
      redisIds.forEach(id -> repository.deleteById(id.getId()));
    });
  }

  @Override
  public void clearCache() {
    log.info("Start clearing the Redis cache");
    coreRedisCacheRepositories.forEach(this::deleteAllByPrefixEnv);
    evictCacheForAllRedisCacheManagers();
    log.info("Finish clearing the Redis cache");
  }

  private <T extends RedisId> void deleteAllByPrefixEnv(CoreRedisCacheRepository<T> repository) {
    List<T> redisIds = repository.findAllByPrefixEnv();
    repository.deleteAll(redisIds);
  }

  @Override
  public void evictLocalCaches() {
    log.info("Start evicting local caffeine caches");
    caffeine1HourCacheManager.getCacheNames()
        .forEach(cacheName -> Objects.requireNonNull(caffeine1HourCacheManager.getCache(cacheName)).clear());
    log.info("Finish evicting local caffeine caches");
  }

  private void evictCacheForAllRedisCacheManagers() {
    List.of(redis5DaysCacheManager)
        .forEach(this::evictCacheForCacheManager);
  }

  private void evictCacheForCacheManager(final CacheManager cacheManager) {
    final Collection<String> cacheNames = cacheManager.getCacheNames();
    log.info("Clear all caches '{}'", cacheNames);
    cacheNames.forEach(this::evictCacheName);
  }

  private void evictCacheName(final String cacheName) {
    final RedisConnection connection = RedisConnectionUtils.getConnection(redisConnectionFactory);
    try {
      evictCacheName(cacheName, connection);
    } finally {
      RedisConnectionUtils.releaseConnection(connection, redisConnectionFactory);
    }
  }

  private void evictCacheName(final String cacheName, final RedisConnection connection) {
    final StopWatch timer = new StopWatch("CacheCleanupAdapter.evictCacheName()");

    final String redisCacheName = cacheKeyPrefix.compute(cacheName);
    log.info("Cache to evict: {};", redisCacheName);

    timer.start("scan for keys");
    final Set<String> keys = getKeys(connection, redisCacheName);
    timer.stop();

    final Set<String> keysToDelete = keys.stream().filter(key -> key.startsWith(redisCacheName)).collect(Collectors
        .toSet());
    log.info("Keys were found: {}; Keys to be deleted: {}", keys.size(), keysToDelete.size());
    if (!keysToDelete.isEmpty()) {
      timer.start("cache eviction");
      connection.del(keysToDelete.stream().map(String::getBytes).toArray(byte[][]::new));
      timer.stop();
    }
    log.info("Execution time: {}ms", timer.getTotalTimeMillis());
  }

  private Set<String> getKeys(final RedisConnection connection, final String cacheName) {
    final Set<String> keys = new HashSet<>();
    final String pattern = cacheName + "*";
    log.info("Scan keys pattern: {};", pattern);
    final ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
    final Cursor<?> c = connection.scan(options);
    while (c.hasNext()) {
      keys.add(new String((byte[]) c.next()));
    }
    log.debug("Scan returned keys: {}", keys);
    return keys;
  }

}

package com.fintex.ce.adapter.cache.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig extends CachingConfigurerSupport {

  private static final Duration ONE_HOUR_TTL = Duration.ofHours(1);

  public static final String GET_FX_RATES = "getFxRates";
  public static final String LOAD_TBILLS = "loadTBills";
  public static final String FIND_ALL_BY_HOLDING_ID = "findAllByHoldingId";
  public static final String HISTORICAL_NAV_PRICES = "findAllByHoldingIdHistoricalNavPrices";
  public static final String HISTORICAL_DISTRIBUTIONS = "findAllByHoldingIdHistoricalDistributions";
  public static final String TOP_COMMON_HOLDINGS = "topCommonHoldings";
  public static final String TOP_COMMON_HOLDINGS_STOCK = "topCommonHoldingsStock";
  private static final Duration FIVE_DAYS_TTL = Duration.ofDays(5);
  private static final Duration FIVE_MINUTES_TTL = Duration.ofMinutes(5);

  @Value(value = "${spring.redis.key-prefix}")
  private String springCacheRedisKeyPrefix;
  @Value("${spring.redis.use-key-prefix:false}")
  private boolean springCacheRedisUseKeyPrefix;
  private CacheKeyPrefix cacheKeyPrefix;
  @PostConstruct
  private void onPostConstruct() {
    if (springCacheRedisKeyPrefix != null) {
      springCacheRedisKeyPrefix = springCacheRedisKeyPrefix.trim();
    }
    if (springCacheRedisUseKeyPrefix && springCacheRedisKeyPrefix != null && !springCacheRedisKeyPrefix.isEmpty()) {
      cacheKeyPrefix = cacheName -> springCacheRedisKeyPrefix + "::" + cacheName + "::";
    } else {
      cacheKeyPrefix = CacheKeyPrefix.simple();
    }
  }

  @Bean
  public CacheKeyPrefix getCacheKeyPrefix() {
    return cacheKeyPrefix;
  }

  @Bean
  public CacheManager redis5MinutesCacheManager(final LettuceConnectionFactory lettuceConnectionFactory) {
    return getRedisCacheManager(lettuceConnectionFactory, FIVE_MINUTES_TTL);
  }

  @Bean
  public CacheManager redis5DaysCacheManager(final LettuceConnectionFactory lettuceConnectionFactory) {
    return getRedisCacheManager(lettuceConnectionFactory, FIVE_DAYS_TTL);
  }

  private CacheManager getRedisCacheManager(final LettuceConnectionFactory lettuceConnectionFactory,
      final Duration duration) {
    final Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = getObjectJackson2JsonRedisSerializer();
    final RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(duration)
        .computePrefixWith(cacheKeyPrefix)
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
    redisCacheConfiguration.usePrefix();
    return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(lettuceConnectionFactory)
        .cacheDefaults(redisCacheConfiguration).build();
  }

  private Jackson2JsonRedisSerializer<Object> getObjectJackson2JsonRedisSerializer() {
    ObjectMapper om = new ObjectMapper()
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false)
        .registerModule(new JavaTimeModule());
    return new Jackson2JsonRedisSerializer<>(om, Object.class);
  }

  @Bean
  @Primary
  public CaffeineCacheManager caffeine1HourCacheManager() {
    return createCaffeineCache(ONE_HOUR_TTL);
  }

  private static CaffeineCacheManager createCaffeineCache(final Duration ttl) {
    final CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
    final Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
        .expireAfterWrite(ttl.toMillis(), TimeUnit.MILLISECONDS);
    caffeineCacheManager.setAllowNullValues(true);
    caffeineCacheManager.setCaffeine(cacheBuilder);
    return caffeineCacheManager;
  }
}

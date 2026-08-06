package com.fintex.ce.adapter.cache.fx;

import com.fintex.ce.adapter.cache.config.CacheDataProperties;
import com.fintex.ce.port.webclient.boc.FxRatesFetcher;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Registers the caching proxy around {@link FxRatesFetcher} when {@code cache.data.fx-rates.enabled=true}. The proxy is
 * marked {@link Primary} so calling code receives the cached variant transparently; the underlying
 * {@code bocFxRatesFetcher} bean remains available and is injected here as the delegate. Disabling the flag keeps the
 * original non-caching fetcher as the sole implementation.
 */
@Configuration
@EnableConfigurationProperties(CacheDataProperties.class)
public class FxRatesCacheConfig {

  private static final String BOC_FETCHER_BEAN_NAME = "bocFxRatesFetcher";

  @Bean
  @ConditionalOnProperty(prefix = "cache.data.fx-rates", name = "enabled", havingValue = "true")
  public FxRatesCache fxRatesCache(
      CacheDataProperties properties,
      ObjectProvider<MeterRegistry> meterRegistryProvider) {
    return new CaffeineFxRatesCache(properties.getFxRates(), meterRegistryProvider.getIfAvailable());
  }

  @Bean
  @Primary
  @ConditionalOnProperty(prefix = "cache.data.fx-rates", name = "enabled", havingValue = "true")
  public FxRatesFetcher cachingFxRatesFetcher(
      @Qualifier(BOC_FETCHER_BEAN_NAME) FxRatesFetcher delegate,
      FxRatesCache cache) {
    return new CachingFxRatesFetcher(delegate, cache);
  }
}

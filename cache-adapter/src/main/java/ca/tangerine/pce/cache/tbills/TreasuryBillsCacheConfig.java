package ca.tangerine.pce.cache.tbills;

import ca.tangerine.pce.cache.config.CacheDataProperties;
import ca.tangerine.pce.port.observability.CacheObservability;
import ca.tangerine.pce.port.webclient.mic.TreasuryBillsFetcher;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Registers the caching proxy around {@link TreasuryBillsFetcher} when {@code cache.data.t-bills.enabled=true}. The
 * proxy is marked {@link Primary} so calling code receives the cached variant transparently; the underlying
 * {@code micTreasuryBillsFetcherImpl} bean remains available and is injected here as the delegate. Disabling the flag
 * keeps the original non-caching fetcher as the sole implementation.
 */
@Configuration
@EnableConfigurationProperties(CacheDataProperties.class)
public class TreasuryBillsCacheConfig {

  private static final String DELEGATE_BEAN_NAME = "micTreasuryBillsFetcherImpl";

  @Bean
  @Primary
  @ConditionalOnProperty(prefix = "cache.data.t-bills", name = "enabled", havingValue = "true")
  public TreasuryBillsFetcher cachingTreasuryBillsFetcher(
      @Qualifier(DELEGATE_BEAN_NAME) TreasuryBillsFetcher delegate,
      CacheDataProperties properties,
      CacheObservability cacheObservability) {
    return new CachingTreasuryBillsFetcher(
        delegate,
        properties.getTBills().getRefreshAfter(),
        cacheObservability);
  }
}

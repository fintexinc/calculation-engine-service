package ca.tangerine.pce.observability.cache;

import ca.tangerine.pce.port.observability.CacheStatistics;

import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;

class MicrometerCacheObservabilityTest {

  private static final String CACHE_NAME = "fx-rates";

  private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
  private final MicrometerCacheObservability observability = new MicrometerCacheObservability(meterRegistry);

  @Test
  void shouldPublishHitsAndMissesUnderOneNameSplitByResult_whenCacheIsRegistered() {
    MutableCacheStatistics statistics = new MutableCacheStatistics();
    statistics.hits = 7;
    statistics.misses = 3;
    statistics.size = 10;
    statistics.evictions = 2;
    statistics.evictionWeight = 4;

    observability.registerCache(CACHE_NAME, statistics);

    assertThat(gets(MicrometerCacheObservability.HIT))
        .isNotNull()
        .satisfies(hits -> assertThat(hits.count()).isEqualTo(7.0));
    assertThat(gets(MicrometerCacheObservability.MISS))
        .isNotNull()
        .satisfies(misses -> assertThat(misses.count()).isEqualTo(3.0));
    assertThat(meterRegistry.find(MicrometerCacheObservability.GETS_METER_NAME)
        .tag(MicrometerCacheObservability.CACHE_TAG, CACHE_NAME)
        .functionCounters())
        .as("a hit ratio divides one by their sum, so they must be one meter split by result")
        .hasSize(2);
    assertThat(meterRegistry.find(MicrometerCacheObservability.SIZE_METER_NAME)
        .tag(MicrometerCacheObservability.CACHE_TAG, CACHE_NAME)
        .gauge())
        .isNotNull()
        .satisfies(size -> assertThat(size.value()).isEqualTo(10.0));
    assertThat(counter(MicrometerCacheObservability.EVICTIONS_METER_NAME).count()).isEqualTo(2.0);
    assertThat(counter(MicrometerCacheObservability.EVICTION_WEIGHT_METER_NAME).count()).isEqualTo(4.0);
  }

  /**
   * A cache is registered once and read from then on, so the meters have to follow the cache rather than report the
   * counters it happened to have at registration time.
   */
  @Test
  void shouldTrackLaterCounts_whenTheCacheKeepsBeingUsedAfterRegistration() {
    MutableCacheStatistics statistics = new MutableCacheStatistics();

    observability.registerCache(CACHE_NAME, statistics);
    statistics.hits = 5;
    statistics.misses = 1;
    statistics.size = 6;

    assertThat(gets(MicrometerCacheObservability.HIT).count()).isEqualTo(5.0);
    assertThat(gets(MicrometerCacheObservability.MISS).count()).isEqualTo(1.0);
    assertThat(meterRegistry.find(MicrometerCacheObservability.SIZE_METER_NAME).gauge().value()).isEqualTo(6.0);
  }

  @Test
  void shouldKeepMetersPerCache_whenSeveralCachesAreRegistered() {
    MutableCacheStatistics fxRates = new MutableCacheStatistics();
    fxRates.hits = 2;
    MutableCacheStatistics treasuryBills = new MutableCacheStatistics();
    treasuryBills.hits = 9;

    observability.registerCache(CACHE_NAME, fxRates);
    observability.registerCache("t-bills", treasuryBills);

    assertThat(gets(MicrometerCacheObservability.HIT).count()).isEqualTo(2.0);
    assertThat(meterRegistry.find(MicrometerCacheObservability.GETS_METER_NAME)
        .tag(MicrometerCacheObservability.CACHE_TAG, "t-bills")
        .tag(MicrometerCacheObservability.RESULT_TAG, MicrometerCacheObservability.HIT)
        .functionCounter()
        .count())
        .isEqualTo(9.0);
  }

  private FunctionCounter gets(String result) {
    return meterRegistry.find(MicrometerCacheObservability.GETS_METER_NAME)
        .tag(MicrometerCacheObservability.CACHE_TAG, CACHE_NAME)
        .tag(MicrometerCacheObservability.RESULT_TAG, result)
        .functionCounter();
  }

  private FunctionCounter counter(String meterName) {
    return meterRegistry.find(meterName)
        .tag(MicrometerCacheObservability.CACHE_TAG, CACHE_NAME)
        .functionCounter();
  }

  private static final class MutableCacheStatistics implements CacheStatistics {

    private long size;
    private long hits;
    private long misses;
    private long evictions;
    private long evictionWeight;

    @Override
    public long estimatedSize() {
      return size;
    }

    @Override
    public long hitCount() {
      return hits;
    }

    @Override
    public long missCount() {
      return misses;
    }

    @Override
    public long evictionCount() {
      return evictions;
    }

    @Override
    public long evictionWeight() {
      return evictionWeight;
    }
  }
}

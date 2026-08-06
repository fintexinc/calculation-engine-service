package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.config.CacheDataProperties.FxRatesCacheProperties;
import com.fintex.ce.adapter.cache.fx.CaffeineFxRatesCache;
import com.fintex.ce.adapter.cache.tbills.CachingTreasuryBillsFetcher;
import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Both Caffeine caches publish their statistics as {@code cache.*} meters, which is how cache effectiveness is judged
 * in production. Covered here for both caches at once because the binding — not the caching behaviour, which each
 * cache's own test already covers — is the same claim in both places: a hit and a miss must be distinguishable, the
 * meters must be attributable to a named cache, and a cache built without a registry must still work.
 */
class CacheMetricsBindingTest {

  private static final String GETS_METER = "cache.gets";
  private static final String CACHE_TAG = "cache";
  private static final String RESULT_TAG = "result";

  private static final CurrencyExchangePair USD_CAD = new CurrencyExchangePair(Currency.USD, Currency.CAD);
  private static final DateRange RANGE = new DateRange(LocalDate.parse("2025-01-01"), LocalDate.parse("2025-01-03"));

  static Stream<Arguments> caches() {
    return Stream.of(
        Arguments.of(CaffeineFxRatesCache.CACHE_NAME,
            (Consumer<MeterRegistry>) CacheMetricsBindingTest::oneMissThenOneHitOnFxRates),
        Arguments.of(CachingTreasuryBillsFetcher.CACHE_NAME,
            (Consumer<MeterRegistry>) CacheMetricsBindingTest::oneMissThenOneHitOnTreasuryBills));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("caches")
  void shouldPublishHitsAndMissesUnderTheCacheName_whenRegistryIsProvided(
      String cacheName,
      Consumer<MeterRegistry> exercise) {
    MeterRegistry meterRegistry = new SimpleMeterRegistry();

    exercise.accept(meterRegistry);

    assertThat(gets(meterRegistry, cacheName, "miss"))
        .as("a miss is what justifies the upstream call this cache exists to avoid")
        .isNotNull()
        .satisfies(misses -> assertThat(misses.count()).isEqualTo(1.0));
    assertThat(gets(meterRegistry, cacheName, "hit"))
        .isNotNull()
        .satisfies(hits -> assertThat(hits.count()).isEqualTo(1.0));
    assertThat(meterRegistry.find(GETS_METER).tag(CACHE_TAG, cacheName).functionCounters())
        .as("hits and misses must be one meter split by result, not two unrelated names")
        .hasSize(2);
    assertThat(meterRegistry.find("cache.size").tag(CACHE_TAG, cacheName).gauge()).isNotNull();
  }

  @Test
  void shouldNotPublishMeters_whenNoRegistryIsProvided() {
    MeterRegistry meterRegistry = new SimpleMeterRegistry();

    oneMissThenOneHitOnFxRates(null);
    oneMissThenOneHitOnTreasuryBills(null);

    assertThat(meterRegistry.find(GETS_METER).meters())
        .as("a cache constructed without a registry must still serve values, just silently")
        .isEmpty();
  }

  private static void oneMissThenOneHitOnFxRates(MeterRegistry meterRegistry) {
    FxRatesCacheProperties properties = new FxRatesCacheProperties();
    properties.setEnabled(true);
    properties.setMaxEntries(4096);
    CaffeineFxRatesCache cache = new CaffeineFxRatesCache(properties, meterRegistry);
    DateRange singleDay = new DateRange(RANGE.start(), RANGE.start());

    NavigableMap<LocalDate, BigDecimal> loaded = cache.getOrLoad(USD_CAD, singleDay, range -> rates(range));
    NavigableMap<LocalDate, BigDecimal> cached = cache.getOrLoad(USD_CAD, singleDay, range -> {
      throw new AssertionError("second lookup must be served from the cache");
    });

    assertThat(loaded).isEqualTo(cached);
  }

  private static void oneMissThenOneHitOnTreasuryBills(MeterRegistry meterRegistry) {
    TreasuryBillsFetcher delegate = mock(TreasuryBillsFetcher.class);
    NavigableMap<LocalDate, BigDecimal> series = rates(RANGE);
    when(delegate.fetch(Currency.CAD)).thenReturn(series);
    CachingTreasuryBillsFetcher fetcher = new CachingTreasuryBillsFetcher(
        delegate, Duration.ofMinutes(5), meterRegistry);

    assertThat(fetcher.fetch(Currency.CAD)).isSameAs(series);
    assertThat(fetcher.fetch(Currency.CAD)).isSameAs(series);
  }

  private static NavigableMap<LocalDate, BigDecimal> rates(DateRange range) {
    NavigableMap<LocalDate, BigDecimal> rates = new TreeMap<>();
    LocalDate cursor = range.start();
    while (!cursor.isAfter(range.end())) {
      rates.put(cursor, BigDecimal.valueOf(1.35));
      cursor = cursor.plusDays(1);
    }
    return rates;
  }

  private static FunctionCounter gets(MeterRegistry meterRegistry, String cacheName, String result) {
    return meterRegistry.find(GETS_METER)
        .tag(CACHE_TAG, cacheName)
        .tag(RESULT_TAG, result)
        .functionCounter();
  }
}

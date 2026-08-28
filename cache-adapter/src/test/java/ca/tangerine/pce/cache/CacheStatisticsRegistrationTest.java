package ca.tangerine.pce.cache;

import ca.tangerine.pce.cache.config.CacheDataProperties.FxRatesCacheProperties;
import ca.tangerine.pce.cache.fx.CaffeineFxRatesCache;
import ca.tangerine.pce.cache.tbills.CachingTreasuryBillsFetcher;
import ca.tangerine.pce.model.domain.CurrencyExchangePair;
import ca.tangerine.pce.model.domain.calculation.DateRange;
import ca.tangerine.pce.port.observability.CacheObservability;
import ca.tangerine.pce.port.observability.CacheStatistics;
import ca.tangerine.pce.port.webclient.mic.TreasuryBillsFetcher;
import ca.tangerine.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Both Caffeine caches make their effectiveness reportable by registering a live statistics view under their own name,
 * which is how cache effectiveness is judged in production. Covered here for both caches at once because the
 * registration — not the caching behaviour, which each cache's own test already covers — is the same claim in both
 * places: a hit and a miss must be distinguishable, the statistics must be attributable to a named cache, and a cache
 * wired to publish nothing must still work.
 *
 * <p>
 * What the counters turn into is not this module's concern and is covered where the meters are actually registered.
 */
class CacheStatisticsRegistrationTest {

  private static final CurrencyExchangePair USD_CAD = new CurrencyExchangePair(Currency.USD, Currency.CAD);
  private static final DateRange RANGE = new DateRange(LocalDate.parse("2025-01-01"), LocalDate.parse("2025-01-03"));

  static Stream<Arguments> caches() {
    return Stream.of(
        Arguments.of(CaffeineFxRatesCache.CACHE_NAME,
            (Consumer<CacheObservability>) CacheStatisticsRegistrationTest::oneMissThenOneHitOnFxRates),
        Arguments.of(CachingTreasuryBillsFetcher.CACHE_NAME,
            (Consumer<CacheObservability>) CacheStatisticsRegistrationTest::oneMissThenOneHitOnTreasuryBills));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("caches")
  void shouldRegisterHitsAndMissesUnderTheCacheName_whenObservabilityIsProvided(
      String cacheName,
      Consumer<CacheObservability> exercise) {
    RecordingCacheObservability observability = new RecordingCacheObservability();

    exercise.accept(observability);

    assertThat(observability.registered)
        .as("statistics that are not attributable to a named cache cannot be read")
        .containsOnlyKeys(cacheName);
    CacheStatistics statistics = observability.registered.get(cacheName);
    assertThat(statistics.missCount())
        .as("a miss is what justifies the upstream call this cache exists to avoid")
        .isEqualTo(1);
    assertThat(statistics.hitCount()).isEqualTo(1);
    assertThat(statistics.estimatedSize()).isPositive();
    assertThat(statistics.evictionCount()).isZero();
    assertThat(statistics.evictionWeight()).isZero();
  }

  @Test
  void shouldServeValues_whenObservabilityPublishesNothing() {
    oneMissThenOneHitOnFxRates(CacheObservability.NO_OP);
    oneMissThenOneHitOnTreasuryBills(CacheObservability.NO_OP);
  }

  private static void oneMissThenOneHitOnFxRates(CacheObservability observability) {
    FxRatesCacheProperties properties = new FxRatesCacheProperties();
    properties.setEnabled(true);
    properties.setMaxEntries(4096);
    CaffeineFxRatesCache cache = new CaffeineFxRatesCache(properties, observability);
    DateRange singleDay = new DateRange(RANGE.start(), RANGE.start());

    NavigableMap<LocalDate, BigDecimal> loaded = cache.getOrLoad(USD_CAD, singleDay, range -> rates(range));
    NavigableMap<LocalDate, BigDecimal> cached = cache.getOrLoad(USD_CAD, singleDay, range -> {
      throw new AssertionError("second lookup must be served from the cache");
    });

    assertThat(loaded).isEqualTo(cached);
  }

  private static void oneMissThenOneHitOnTreasuryBills(CacheObservability observability) {
    TreasuryBillsFetcher delegate = mock(TreasuryBillsFetcher.class);
    NavigableMap<LocalDate, BigDecimal> series = rates(RANGE);
    when(delegate.fetch(Currency.CAD)).thenReturn(series);
    CachingTreasuryBillsFetcher fetcher = new CachingTreasuryBillsFetcher(
        delegate, Duration.ofMinutes(5), observability);

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

  private static final class RecordingCacheObservability implements CacheObservability {

    private final Map<String, CacheStatistics> registered = new HashMap<>();

    @Override
    public void registerCache(String cacheName, CacheStatistics statistics) {
      registered.put(cacheName, statistics);
    }
  }
}

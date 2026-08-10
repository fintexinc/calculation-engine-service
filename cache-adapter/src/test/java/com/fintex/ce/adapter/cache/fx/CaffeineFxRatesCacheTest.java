package com.fintex.ce.adapter.cache.fx;

import com.fintex.ce.adapter.cache.config.CacheDataProperties.FxRatesCacheProperties;
import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.port.observability.CacheObservability;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class CaffeineFxRatesCacheTest {

  private static final CurrencyExchangePair USD_CAD = new CurrencyExchangePair(Currency.USD, Currency.CAD);

  private CaffeineFxRatesCache cache;

  @BeforeEach
  void setUp() {
    FxRatesCacheProperties properties = new FxRatesCacheProperties();
    properties.setEnabled(true);
    properties.setMaxEntries(4096);
    cache = new CaffeineFxRatesCache(properties, CacheObservability.NO_OP);
  }

  @Test
  void shouldLoadEntireRangeFromDelegate_whenCacheIsEmpty() {
    DateRange range = range("2025-01-01", "2025-12-31");
    CountingLoader loader = new CountingLoader(ratesFor(range));

    NavigableMap<LocalDate, BigDecimal> result = cache.getOrLoad(USD_CAD, range, loader);

    assertThat(result).hasSize(365);
    assertThat(loader.invocations()).isEqualTo(1);
    assertThat(loader.lastRange()).isEqualTo(range);
  }

  @Test
  void shouldServeSubRangeFromCache_whenCoveredByPreviousCall() {
    DateRange full = range("2025-01-01", "2025-12-31");
    cache.getOrLoad(USD_CAD, full, new CountingLoader(ratesFor(full)));

    DateRange subRange = range("2025-06-01", "2025-07-31");
    CountingLoader loader = new CountingLoader(new TreeMap<>());

    NavigableMap<LocalDate, BigDecimal> result = cache.getOrLoad(USD_CAD, subRange, loader);

    assertThat(loader.invocations()).isZero();
    assertThat(result.firstKey()).isEqualTo(LocalDate.parse("2025-06-01"));
    assertThat(result.lastKey()).isEqualTo(LocalDate.parse("2025-07-31"));
    assertThat(result).hasSize(61);
  }

  @Test
  void shouldFetchOnlyMissingSubRange_whenRequestExtendsBeyondCachedRange() {
    DateRange firstRange = range("2025-01-01", "2025-12-31");
    cache.getOrLoad(USD_CAD, firstRange, new CountingLoader(ratesFor(firstRange)));

    DateRange secondRange = range("2024-06-01", "2025-03-31");
    DateRange expectedGap = range("2024-06-01", "2024-12-31");
    CountingLoader loader = new CountingLoader(ratesFor(expectedGap));

    NavigableMap<LocalDate, BigDecimal> result = cache.getOrLoad(USD_CAD, secondRange, loader);

    assertThat(loader.invocations()).isEqualTo(1);
    assertThat(loader.lastRange()).isEqualTo(expectedGap);
    assertThat(result.firstKey()).isEqualTo(LocalDate.parse("2024-06-01"));
    assertThat(result.lastKey()).isEqualTo(LocalDate.parse("2025-03-31"));
  }

  @Test
  void shouldFetchTwoGaps_whenRequestSpansBothEndsOfCachedRange() {
    DateRange cached = range("2025-01-01", "2025-12-31");
    cache.getOrLoad(USD_CAD, cached, new CountingLoader(ratesFor(cached)));

    DateRange wide = range("2024-10-01", "2026-02-28");
    CountingLoader loader = new CountingLoader(new TreeMap<>());

    cache.getOrLoad(USD_CAD, wide, loader);

    assertThat(loader.invocations()).isEqualTo(2);
  }

  @Test
  void shouldIsolateCacheEntries_whenDifferentPairsQueried() {
    CurrencyExchangePair eurCad = new CurrencyExchangePair(Currency.EUR, Currency.CAD);
    DateRange range = range("2025-01-01", "2025-01-31");

    cache.getOrLoad(USD_CAD, range, new CountingLoader(ratesFor(range)));

    CountingLoader loader = new CountingLoader(ratesFor(range));
    cache.getOrLoad(eurCad, range, loader);

    assertThat(loader.invocations()).isEqualTo(1);
  }

  @Test
  void shouldStoreInversePairSeparately_whenCacheIsDirectionAgnostic() {
    CurrencyExchangePair cadUsd = new CurrencyExchangePair(Currency.CAD, Currency.USD);
    DateRange range = range("2025-01-01", "2025-01-31");

    cache.getOrLoad(USD_CAD, range, new CountingLoader(ratesFor(range)));

    CountingLoader inverseLoader = new CountingLoader(ratesFor(range));
    cache.getOrLoad(cadUsd, range, inverseLoader);

    assertThat(inverseLoader.invocations()).isEqualTo(1);
  }

  @Test
  void shouldNegativeCacheNonTradingDays_whenLoaderReturnsPartial() {
    DateRange range = range("2025-01-01", "2025-01-10");
    NavigableMap<LocalDate, BigDecimal> sparse = new TreeMap<>();
    sparse.put(LocalDate.parse("2025-01-02"), new BigDecimal("1.350"));
    sparse.put(LocalDate.parse("2025-01-06"), new BigDecimal("1.351"));

    CountingLoader firstLoader = new CountingLoader(sparse);
    cache.getOrLoad(USD_CAD, range, firstLoader);
    assertThat(firstLoader.invocations()).isEqualTo(1);

    CountingLoader secondLoader = new CountingLoader(sparse);
    NavigableMap<LocalDate, BigDecimal> result = cache.getOrLoad(USD_CAD, range, secondLoader);

    assertThat(secondLoader.invocations()).isZero();
    assertThat(result).hasSize(2);
    assertThat(result).containsKeys(LocalDate.parse("2025-01-02"), LocalDate.parse("2025-01-06"));
  }

  @Test
  void shouldTrimResultToGap_whenLoaderReturnsDatesBeyondRequestedRange() {
    DateRange requested = range("2025-06-01", "2025-06-30");
    NavigableMap<LocalDate, BigDecimal> wide = ratesFor(range("2025-01-01", "2025-12-31"));
    Function<DateRange, NavigableMap<LocalDate, BigDecimal>> wideLoader = gap -> wide;

    NavigableMap<LocalDate, BigDecimal> result = cache.getOrLoad(USD_CAD, requested, wideLoader);

    assertThat(result.firstKey()).isEqualTo(LocalDate.parse("2025-06-01"));
    assertThat(result.lastKey()).isEqualTo(LocalDate.parse("2025-06-30"));
    assertThat(result).hasSize(30);
  }

  @Test
  void shouldNotPolluteCacheWithAbsentEntries_whenLoaderReturnsEmptyForEntireGap() {
    DateRange range = range("2025-01-01", "2025-12-31");

    CountingLoader emptyLoader = new CountingLoader(new TreeMap<>());
    cache.getOrLoad(USD_CAD, range, emptyLoader);
    assertThat(emptyLoader.invocations()).isEqualTo(1);

    CountingLoader fullLoader = new CountingLoader(ratesFor(range));
    NavigableMap<LocalDate, BigDecimal> result = cache.getOrLoad(USD_CAD, range, fullLoader);

    assertThat(fullLoader.invocations()).isEqualTo(1);
    assertThat(fullLoader.lastRange()).isEqualTo(range);
    assertThat(result).hasSize(365);
  }

  @Test
  void shouldCacheTodayAndAvoidRefetch_whenRangeIncludesToday() {
    LocalDate today = LocalDate.now();
    DateRange range = new DateRange(today.minusDays(1), today);

    CountingLoader first = new CountingLoader(ratesFor(range));
    cache.getOrLoad(USD_CAD, range, first);
    assertThat(first.invocations()).isEqualTo(1);

    CountingLoader second = new CountingLoader(ratesFor(range));
    cache.getOrLoad(USD_CAD, range, second);
    assertThat(second.invocations()).isZero();
  }

  @Test
  void shouldRefetchToday_whenLoaderInitiallyReturnedNoRateForToday() {
    LocalDate today = LocalDate.now();
    LocalDate yesterday = today.minusDays(1);
    DateRange range = new DateRange(yesterday, today);

    NavigableMap<LocalDate, BigDecimal> beforeBocPublishes = new TreeMap<>();
    beforeBocPublishes.put(yesterday, new BigDecimal("1.350"));
    CountingLoader first = new CountingLoader(beforeBocPublishes);
    cache.getOrLoad(USD_CAD, range, first);
    assertThat(first.invocations()).isEqualTo(1);

    NavigableMap<LocalDate, BigDecimal> afterBocPublishes = new TreeMap<>(beforeBocPublishes);
    afterBocPublishes.put(today, new BigDecimal("1.355"));
    CountingLoader second = new CountingLoader(afterBocPublishes);
    NavigableMap<LocalDate, BigDecimal> result = cache.getOrLoad(USD_CAD, range, second);

    assertThat(second.invocations()).isEqualTo(1);
    assertThat(second.lastRange()).isEqualTo(new DateRange(today, today));
    assertThat(result).containsEntry(today, new BigDecimal("1.355"));
  }

  private static DateRange range(String from, String to) {
    return new DateRange(LocalDate.parse(from), LocalDate.parse(to));
  }

  private static NavigableMap<LocalDate, BigDecimal> ratesFor(DateRange range) {
    NavigableMap<LocalDate, BigDecimal> rates = new TreeMap<>();
    LocalDate cursor = range.start();
    int counter = 0;
    while (!cursor.isAfter(range.end())) {
      rates.put(cursor, BigDecimal.valueOf(1.00 + (counter++ % 100) / 1000.0));
      cursor = cursor.plusDays(1);
    }
    return rates;
  }

  private static final class CountingLoader implements Function<DateRange, NavigableMap<LocalDate, BigDecimal>> {

    private final NavigableMap<LocalDate, BigDecimal> source;
    private final AtomicInteger invocations = new AtomicInteger();
    private DateRange lastRange;

    CountingLoader(NavigableMap<LocalDate, BigDecimal> source) {
      this.source = source;
    }

    @Override
    public NavigableMap<LocalDate, BigDecimal> apply(DateRange range) {
      invocations.incrementAndGet();
      lastRange = range;
      return new TreeMap<>(source.subMap(range.start(), true, range.end(), true));
    }

    int invocations() {
      return invocations.get();
    }

    DateRange lastRange() {
      return lastRange;
    }
  }
}

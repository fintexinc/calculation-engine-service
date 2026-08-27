package ca.tangerine.pce.cache.fx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.tangerine.pce.cache.config.CacheDataProperties.FxRatesCacheProperties;
import ca.tangerine.pce.model.domain.CurrencyExchangePair;
import ca.tangerine.pce.model.domain.calculation.DateRange;
import ca.tangerine.pce.port.observability.CacheObservability;
import ca.tangerine.pce.port.webclient.boc.FxRatesFetcher;
import ca.tangerine.wm.commons.domain.currency.Currency;

@ExtendWith(MockitoExtension.class)
class CachingFxRatesFetcherTest {

  private static final CurrencyExchangePair USD_CAD = new CurrencyExchangePair(Currency.USD, Currency.CAD);
  private static final CurrencyExchangePair CAD_USD = new CurrencyExchangePair(Currency.CAD, Currency.USD);
  private static final CurrencyExchangePair EUR_USD = new CurrencyExchangePair(Currency.EUR, Currency.USD);
  private static final CurrencyExchangePair USD_EUR = new CurrencyExchangePair(Currency.USD, Currency.EUR);
  private static final DateRange RANGE = new DateRange(LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31"));

  @Mock
  private FxRatesFetcher delegate;

  @Mock
  private FxRatesCache cache;

  @InjectMocks
  private CachingFxRatesFetcher fetcher;

  @Test
  void shouldDelegateThroughCache_whenRangeIsBounded() {
    when(delegate.canonicalDirection(USD_CAD)).thenReturn(USD_CAD);
    NavigableMap<LocalDate, BigDecimal> expected = new TreeMap<>();
    expected.put(RANGE.start(), BigDecimal.ONE);
    when(cache.getOrLoad(eq(USD_CAD), eq(RANGE), any())).thenReturn(expected);

    NavigableMap<LocalDate, BigDecimal> result = fetcher.fetch(USD_CAD, RANGE);

    assertThat(result).isSameAs(expected);
    verify(delegate, never()).fetch(any(), any());
  }

  @Test
  void shouldInvokeDelegateForMissingGap_whenCacheLoaderCallsBack() {
    when(delegate.canonicalDirection(USD_CAD)).thenReturn(USD_CAD);
    ArgumentCaptor<Function<DateRange, NavigableMap<LocalDate, BigDecimal>>> loaderCaptor = loaderCaptor();
    when(cache.getOrLoad(eq(USD_CAD), eq(RANGE), loaderCaptor.capture())).thenReturn(new TreeMap<>());

    fetcher.fetch(USD_CAD, RANGE);

    DateRange gap = new DateRange(LocalDate.parse("2025-03-01"), LocalDate.parse("2025-03-31"));
    loaderCaptor.getValue().apply(gap);
    verify(delegate).fetch(USD_CAD, gap);
  }

  @Test
  void shouldBypassCache_whenRangeIsNull() {
    NavigableMap<LocalDate, BigDecimal> delegateResult = new TreeMap<>();
    when(delegate.fetch(USD_CAD, null)).thenReturn(delegateResult);

    NavigableMap<LocalDate, BigDecimal> result = fetcher.fetch(USD_CAD, null);

    assertThat(result).isSameAs(delegateResult);
    verify(cache, never()).getOrLoad(any(), any(), any());
  }

  @Test
  void shouldBypassCache_whenRangeIsUnbounded() {
    DateRange unbounded = new DateRange(null, null);
    when(delegate.fetch(USD_CAD, unbounded)).thenReturn(new TreeMap<>());

    fetcher.fetch(USD_CAD, unbounded);

    verify(cache, never()).getOrLoad(any(), any(), any());
  }

  @ParameterizedTest
  @MethodSource("singleBoundRanges")
  void shouldBypassCache_whenRangeHasSingleBound(DateRange range) {
    NavigableMap<LocalDate, BigDecimal> delegateResult = new TreeMap<>();
    when(delegate.fetch(USD_CAD, range)).thenReturn(delegateResult);

    NavigableMap<LocalDate, BigDecimal> result = fetcher.fetch(USD_CAD, range);

    assertThat(result).isSameAs(delegateResult);
    verify(cache, never()).getOrLoad(any(), any(), any());
  }

  @Test
  void shouldReturnEmpty_whenStartIsAfterEnd() {
    DateRange inverted = new DateRange(LocalDate.parse("2025-12-31"), LocalDate.parse("2025-01-01"));

    NavigableMap<LocalDate, BigDecimal> result = fetcher.fetch(USD_CAD, inverted);

    assertThat(result).isEmpty();
    verify(cache, never()).getOrLoad(any(), any(), any());
    verify(delegate, never()).fetch(any(), any());
  }

  @Test
  void shouldCanonicalizeViaDelegate_whenRequestedPairIsNotCanonical() {
    when(delegate.canonicalDirection(CAD_USD)).thenReturn(USD_CAD);
    ArgumentCaptor<Function<DateRange, NavigableMap<LocalDate, BigDecimal>>> loaderCaptor = loaderCaptor();
    when(cache.getOrLoad(eq(USD_CAD), eq(RANGE), loaderCaptor.capture())).thenReturn(new TreeMap<>());

    fetcher.fetch(CAD_USD, RANGE);

    verify(cache, never()).getOrLoad(eq(CAD_USD), any(), any());
    DateRange gap = new DateRange(LocalDate.parse("2025-03-01"), LocalDate.parse("2025-03-31"));
    loaderCaptor.getValue().apply(gap);
    verify(delegate).fetch(USD_CAD, gap);
  }

  @Test
  void shouldCanonicalizeAnyInversePair_whenDelegateReportsADifferentCanonicalDirection() {
    when(delegate.canonicalDirection(USD_EUR)).thenReturn(EUR_USD);
    ArgumentCaptor<Function<DateRange, NavigableMap<LocalDate, BigDecimal>>> loaderCaptor = loaderCaptor();
    when(cache.getOrLoad(eq(EUR_USD), eq(RANGE), loaderCaptor.capture())).thenReturn(new TreeMap<>());

    fetcher.fetch(USD_EUR, RANGE);

    verify(cache, never()).getOrLoad(eq(USD_EUR), any(), any());
    DateRange gap = new DateRange(LocalDate.parse("2025-06-01"), LocalDate.parse("2025-06-30"));
    loaderCaptor.getValue().apply(gap);
    verify(delegate).fetch(EUR_USD, gap);
  }

  @Test
  void shouldInvertCanonicalRatesWithMatchingScale_whenRequestedPairIsNotCanonical() {
    when(delegate.canonicalDirection(CAD_USD)).thenReturn(USD_CAD);
    LocalDate day = LocalDate.parse("2025-01-15");
    BigDecimal canonicalRate = new BigDecimal("1.35");
    NavigableMap<LocalDate, BigDecimal> canonicalRates = new TreeMap<>();
    canonicalRates.put(day, canonicalRate);
    when(cache.getOrLoad(eq(USD_CAD), eq(RANGE), any())).thenReturn(canonicalRates);

    NavigableMap<LocalDate, BigDecimal> result = fetcher.fetch(CAD_USD, RANGE);

    BigDecimal expected = BigDecimal.ONE.divide(canonicalRate, 10, RoundingMode.HALF_UP);
    assertThat(result.get(day)).isEqualByComparingTo(expected);
  }

  @Test
  void shouldPreserveCanonicalRateExactly_whenInverseRequestSeedsCacheBeforeCanonicalRequest() {
    FxRatesCacheProperties properties = new FxRatesCacheProperties();
    properties.setEnabled(true);
    properties.setMaxEntries(4096);
    CaffeineFxRatesCache realCache = new CaffeineFxRatesCache(properties, CacheObservability.NO_OP);
    CachingFxRatesFetcher realFetcher = new CachingFxRatesFetcher(delegate, realCache);

    when(delegate.canonicalDirection(USD_CAD)).thenReturn(USD_CAD);
    when(delegate.canonicalDirection(CAD_USD)).thenReturn(USD_CAD);

    DateRange singleDayRange = new DateRange(LocalDate.parse("2025-01-15"), LocalDate.parse("2025-01-15"));
    LocalDate day = singleDayRange.start();
    BigDecimal exactCanonicalRate = new BigDecimal("1.35");
    NavigableMap<LocalDate, BigDecimal> canonicalResponse = new TreeMap<>();
    canonicalResponse.put(day, exactCanonicalRate);
    when(delegate.fetch(USD_CAD, singleDayRange)).thenReturn(canonicalResponse);

    realFetcher.fetch(CAD_USD, singleDayRange);
    NavigableMap<LocalDate, BigDecimal> canonicalResult = realFetcher.fetch(USD_CAD, singleDayRange);

    assertThat(canonicalResult.get(day)).isEqualByComparingTo(exactCanonicalRate);
    verify(delegate, never()).fetch(eq(CAD_USD), any());
  }

  private static Stream<DateRange> singleBoundRanges() {
    return Stream.of(
        new DateRange(RANGE.start(), null),
        new DateRange(null, RANGE.end()));
  }

  @SuppressWarnings("unchecked")
  private static ArgumentCaptor<Function<DateRange, NavigableMap<LocalDate, BigDecimal>>> loaderCaptor() {
    return ArgumentCaptor.forClass(Function.class);
  }
}

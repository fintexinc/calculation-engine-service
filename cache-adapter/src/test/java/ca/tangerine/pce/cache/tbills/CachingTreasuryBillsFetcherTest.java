package ca.tangerine.pce.cache.tbills;

import ca.tangerine.pce.port.observability.CacheObservability;
import ca.tangerine.pce.port.webclient.mic.TreasuryBillsFetcher;
import ca.tangerine.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachingTreasuryBillsFetcherTest {

  private final TreasuryBillsFetcher delegate = mock(TreasuryBillsFetcher.class);

  @Test
  void shouldDelegateOnlyOnce_whenFetchCalledRepeatedlyForSameCurrencyWithinTtl() {
    NavigableMap<LocalDate, BigDecimal> cadSeries = singleEntry(LocalDate.parse("2020-01-31"),
        BigDecimal.valueOf(0.05));
    when(delegate.fetch(Currency.CAD)).thenReturn(cadSeries);
    CachingTreasuryBillsFetcher fetcher = new CachingTreasuryBillsFetcher(delegate, Duration.ofMinutes(1),
        CacheObservability.NO_OP);

    NavigableMap<LocalDate, BigDecimal> first = fetcher.fetch(Currency.CAD);
    NavigableMap<LocalDate, BigDecimal> second = fetcher.fetch(Currency.CAD);
    NavigableMap<LocalDate, BigDecimal> third = fetcher.fetch(Currency.CAD);

    assertThat(first).isSameAs(cadSeries);
    assertThat(second).isSameAs(cadSeries);
    assertThat(third).isSameAs(cadSeries);
    verify(delegate, times(1)).fetch(Currency.CAD);
  }

  @Test
  void shouldKeepEntriesIsolatedPerCurrency_whenFetchCalledForDifferentCurrencies() {
    NavigableMap<LocalDate, BigDecimal> cadSeries = singleEntry(LocalDate.parse("2020-01-31"),
        BigDecimal.valueOf(0.05));
    NavigableMap<LocalDate, BigDecimal> usdSeries = singleEntry(LocalDate.parse("2020-01-31"),
        BigDecimal.valueOf(0.06));
    when(delegate.fetch(Currency.CAD)).thenReturn(cadSeries);
    when(delegate.fetch(Currency.USD)).thenReturn(usdSeries);
    CachingTreasuryBillsFetcher fetcher = new CachingTreasuryBillsFetcher(delegate, Duration.ofMinutes(1),
        CacheObservability.NO_OP);

    NavigableMap<LocalDate, BigDecimal> cad = fetcher.fetch(Currency.CAD);
    NavigableMap<LocalDate, BigDecimal> usd = fetcher.fetch(Currency.USD);
    NavigableMap<LocalDate, BigDecimal> cadAgain = fetcher.fetch(Currency.CAD);
    NavigableMap<LocalDate, BigDecimal> usdAgain = fetcher.fetch(Currency.USD);

    assertThat(cad).isSameAs(cadSeries);
    assertThat(usd).isSameAs(usdSeries);
    assertThat(cadAgain).isSameAs(cadSeries);
    assertThat(usdAgain).isSameAs(usdSeries);
    verify(delegate, times(1)).fetch(Currency.CAD);
    verify(delegate, times(1)).fetch(Currency.USD);
  }

  @Test
  void shouldRefetch_whenCacheExpired() throws InterruptedException {
    NavigableMap<LocalDate, BigDecimal> a = singleEntry(LocalDate.parse("2020-01-31"), BigDecimal.valueOf(0.05));
    NavigableMap<LocalDate, BigDecimal> b = singleEntry(LocalDate.parse("2020-01-31"), BigDecimal.valueOf(0.06));
    when(delegate.fetch(Currency.CAD)).thenReturn(a, b);
    CachingTreasuryBillsFetcher fetcher = new CachingTreasuryBillsFetcher(delegate, Duration.ofMillis(50),
        CacheObservability.NO_OP);

    NavigableMap<LocalDate, BigDecimal> first = fetcher.fetch(Currency.CAD);
    Thread.sleep(100);
    NavigableMap<LocalDate, BigDecimal> second = fetcher.fetch(Currency.CAD);

    assertThat(first).isSameAs(a);
    assertThat(second).isSameAs(b);
    verify(delegate, times(2)).fetch(Currency.CAD);
  }

  private static NavigableMap<LocalDate, BigDecimal> singleEntry(LocalDate date, BigDecimal value) {
    TreeMap<LocalDate, BigDecimal> series = new TreeMap<>();
    series.put(date, value);
    return series;
  }
}
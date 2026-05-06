package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.rates.TreasuryRates;
import com.fintex.wm.commons.domain.reference.TreasuryRateReturn;
import com.fintex.wm.commons.domain.reference.TreasuryRateReturnsDatapoint;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TBillsFetcherImplTest {

  private static final String ENDPOINT = "/api/v1/wealth/reference/treasury-rates";

  @Test
  void shouldReturnEmptySeriesForEachCurrency_whenSecurityMasterReturnsNullBody() {
    SecurityMasterWebClient client = mock(SecurityMasterWebClient.class);
    TBillsFetcherImpl fetcher = new TBillsFetcherImpl(client, ENDPOINT);

    when(client.get(eq(ENDPOINT), eq(TreasuryRates.class))).thenReturn(null);

    Map<Currency, NavigableMap<LocalDate, BigDecimal>> result = fetcher.fetch();

    assertTrue(result.get(Currency.CAD).isEmpty());
    assertTrue(result.get(Currency.USD).isEmpty());
  }

  @Test
  void shouldReturnEmptySeriesForEachCurrency_whenSecurityMasterReturnsEmptyEntryList() {
    SecurityMasterWebClient client = mock(SecurityMasterWebClient.class);
    TBillsFetcherImpl fetcher = new TBillsFetcherImpl(client, ENDPOINT);

    when(client.get(eq(ENDPOINT), eq(TreasuryRates.class))).thenReturn(buildResponse(List.of()));

    Map<Currency, NavigableMap<LocalDate, BigDecimal>> result = fetcher.fetch();

    assertTrue(result.get(Currency.CAD).isEmpty());
    assertTrue(result.get(Currency.USD).isEmpty());
  }

  @Test
  void shouldKeyEachCurrencyByItsOwnSeries_whenFetchingCombinedResponse() {
    SecurityMasterWebClient client = mock(SecurityMasterWebClient.class);
    TBillsFetcherImpl fetcher = new TBillsFetcherImpl(client, ENDPOINT);

    List<TreasuryRateReturn> entries = List.of(
        entry(LocalDate.of(2025, 1, 31), "0.0035", "0.0030"),
        entry(LocalDate.of(2025, 2, 28), "0.0036", null),
        entry(LocalDate.of(2025, 3, 31), null, "0.0031"));

    when(client.get(eq(ENDPOINT), eq(TreasuryRates.class))).thenReturn(buildResponse(entries));

    Map<Currency, NavigableMap<LocalDate, BigDecimal>> result = fetcher.fetch();

    NavigableMap<LocalDate, BigDecimal> cad = result.get(Currency.CAD);
    assertEquals(2, cad.size());
    assertEquals(new BigDecimal("0.0030"), cad.get(LocalDate.of(2025, 1, 31)));
    assertEquals(new BigDecimal("0.0031"), cad.get(LocalDate.of(2025, 3, 31)));

    NavigableMap<LocalDate, BigDecimal> usd = result.get(Currency.USD);
    assertEquals(2, usd.size());
    assertEquals(new BigDecimal("0.0035"), usd.get(LocalDate.of(2025, 1, 31)));
    assertEquals(new BigDecimal("0.0036"), usd.get(LocalDate.of(2025, 2, 28)));
  }

  @Test
  void shouldReturnEmptySeriesForUnsupportedCurrencies_whenFetchingCombinedResponse() {
    SecurityMasterWebClient client = mock(SecurityMasterWebClient.class);
    TBillsFetcherImpl fetcher = new TBillsFetcherImpl(client, ENDPOINT);

    when(client.get(eq(ENDPOINT), eq(TreasuryRates.class))).thenReturn(buildResponse(List.of(
        entry(LocalDate.of(2025, 1, 31), "0.0035", "0.0030"))));

    Map<Currency, NavigableMap<LocalDate, BigDecimal>> result = fetcher.fetch();

    // Every Currency value must yield a non-null map so callers can `.get(currency)` without null-checking.
    for (Currency currency : Currency.values()) {
      assertTrue(result.get(currency) != null,
          "Expected non-null series for currency " + currency + " but got null");
    }
    // Currencies SMS does not supply must yield an empty (not null) series.
    assertTrue(result.get(Currency.EUR).isEmpty());
  }

  private TreasuryRates buildResponse(List<TreasuryRateReturn> entries) {
    TreasuryRateReturnsDatapoint datapoint = new TreasuryRateReturnsDatapoint();
    datapoint.setReturns(new ArrayList<>(entries));
    TreasuryRates rates = new TreasuryRates();
    rates.setReturns(datapoint);
    return rates;
  }

  private TreasuryRateReturn entry(LocalDate date, String us, String cad) {
    TreasuryRateReturn entry = new TreasuryRateReturn();
    entry.setDate(date);
    Map<Currency, BigDecimal> rates = new EnumMap<>(Currency.class);
    if (us != null) {
      rates.put(Currency.USD, new BigDecimal(us));
    }
    if (cad != null) {
      rates.put(Currency.CAD, new BigDecimal(cad));
    }
    entry.setRates(rates);
    return entry;
  }
}
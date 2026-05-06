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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SmsTreasuryBillsFetcherImplTest {

  private static final String ENDPOINT = "/api/v1/wealth/reference/treasury-rates";

  @Test
  void shouldReturnEmptySeries_whenSecurityMasterReturnsNullBody() {
    SecurityMasterWebClient client = mock(SecurityMasterWebClient.class);
    SmsTreasuryBillsFetcherImpl fetcher = new SmsTreasuryBillsFetcherImpl(client, ENDPOINT);
    when(client.get(eq(ENDPOINT), eq(TreasuryRates.class))).thenReturn(null);

    NavigableMap<LocalDate, BigDecimal> cad = fetcher.fetch(Currency.CAD);
    NavigableMap<LocalDate, BigDecimal> usd = fetcher.fetch(Currency.USD);

    assertThat(cad).isEmpty();
    assertThat(usd).isEmpty();
  }

  @Test
  void shouldReturnEmptySeries_whenSecurityMasterReturnsEmptyEntryList() {
    SecurityMasterWebClient client = mock(SecurityMasterWebClient.class);
    SmsTreasuryBillsFetcherImpl fetcher = new SmsTreasuryBillsFetcherImpl(client, ENDPOINT);
    when(client.get(eq(ENDPOINT), eq(TreasuryRates.class))).thenReturn(buildResponse(List.of()));

    assertThat(fetcher.fetch(Currency.CAD)).isEmpty();
  }

  @Test
  void shouldReturnOnlyRequestedCurrencysSeries_whenFetchingCombinedResponse() {
    SecurityMasterWebClient client = mock(SecurityMasterWebClient.class);
    SmsTreasuryBillsFetcherImpl fetcher = new SmsTreasuryBillsFetcherImpl(client, ENDPOINT);
    List<TreasuryRateReturn> entries = List.of(
        entry(LocalDate.of(2025, 1, 31), "0.0035", "0.0030"),
        entry(LocalDate.of(2025, 2, 28), "0.0036", null),
        entry(LocalDate.of(2025, 3, 31), null, "0.0031"));
    when(client.get(eq(ENDPOINT), eq(TreasuryRates.class))).thenReturn(buildResponse(entries));

    NavigableMap<LocalDate, BigDecimal> cad = fetcher.fetch(Currency.CAD);
    NavigableMap<LocalDate, BigDecimal> usd = fetcher.fetch(Currency.USD);

    assertThat(cad).containsOnlyKeys(LocalDate.of(2025, 1, 31), LocalDate.of(2025, 3, 31));
    assertThat(cad).containsEntry(LocalDate.of(2025, 1, 31), new BigDecimal("0.0030"));
    assertThat(cad).containsEntry(LocalDate.of(2025, 3, 31), new BigDecimal("0.0031"));
    assertThat(usd).containsOnlyKeys(LocalDate.of(2025, 1, 31), LocalDate.of(2025, 2, 28));
    assertThat(usd).containsEntry(LocalDate.of(2025, 1, 31), new BigDecimal("0.0035"));
    assertThat(usd).containsEntry(LocalDate.of(2025, 2, 28), new BigDecimal("0.0036"));
  }

  @Test
  void shouldReturnEmptySeries_whenCurrencyIsUnsupported() {
    SecurityMasterWebClient client = mock(SecurityMasterWebClient.class);
    SmsTreasuryBillsFetcherImpl fetcher = new SmsTreasuryBillsFetcherImpl(client, ENDPOINT);
    when(client.get(eq(ENDPOINT), eq(TreasuryRates.class))).thenReturn(buildResponse(List.of(
        entry(LocalDate.of(2025, 1, 31), "0.0035", "0.0030"))));

    assertThat(fetcher.fetch(Currency.EUR)).isEmpty();
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

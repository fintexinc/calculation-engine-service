package ca.tangerine.pce.webclient.mic.fetcher;

import org.springframework.core.ParameterizedTypeReference;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.tangerine.pce.webclient.mic.client.MarketInvestmentCatalogueWebClient;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.rates.DateRateValue;

class MicTreasuryBillsFetcherImplTest {

  private static final String ENDPOINT = "/api/v1/wealth/reference/treasury-rates";

  @SuppressWarnings("unchecked")
  @Test
  void shouldReturnEmptySeries_whenMarketInvestmentCatalogueReturnsNullBody() {
    MarketInvestmentCatalogueWebClient client = mock(MarketInvestmentCatalogueWebClient.class);
    MicTreasuryBillsFetcherImpl fetcher = new MicTreasuryBillsFetcherImpl(client, ENDPOINT);
    when(client.get(eq(ENDPOINT + "/CAD"), any(ParameterizedTypeReference.class))).thenReturn(null);

    NavigableMap<LocalDate, BigDecimal> series = fetcher.fetch(Currency.CAD);

    assertThat(series).isEmpty();
  }

  @SuppressWarnings("unchecked")
  @Test
  void shouldReturnEmptySeries_whenMarketInvestmentCatalogueReturnsEmptyList() {
    MarketInvestmentCatalogueWebClient client = mock(MarketInvestmentCatalogueWebClient.class);
    MicTreasuryBillsFetcherImpl fetcher = new MicTreasuryBillsFetcherImpl(client, ENDPOINT);
    when(client.get(eq(ENDPOINT + "/CAD"), any(ParameterizedTypeReference.class))).thenReturn(List.of());

    NavigableMap<LocalDate, BigDecimal> series = fetcher.fetch(Currency.CAD);

    assertThat(series).isEmpty();
  }

  @SuppressWarnings("unchecked")
  @Test
  void shouldRequestCurrencyAsPathSegment_whenInvokingMarketInvestmentCatalogue() {
    MarketInvestmentCatalogueWebClient client = mock(MarketInvestmentCatalogueWebClient.class);
    MicTreasuryBillsFetcherImpl fetcher = new MicTreasuryBillsFetcherImpl(client, ENDPOINT);
    when(client.get(eq(ENDPOINT + "/USD"), any(ParameterizedTypeReference.class)))
        .thenReturn(List.of(new DateRateValue(LocalDate.of(2025, 1, 31), new BigDecimal("0.0035"))));

    fetcher.fetch(Currency.USD);

    verify(client).get(eq(ENDPOINT + "/USD"), any(ParameterizedTypeReference.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void shouldBuildSortedSeries_whenMarketInvestmentCatalogueResponds() {
    MarketInvestmentCatalogueWebClient client = mock(MarketInvestmentCatalogueWebClient.class);
    MicTreasuryBillsFetcherImpl fetcher = new MicTreasuryBillsFetcherImpl(client, ENDPOINT);

    when(client.get(eq(ENDPOINT + "/USD"), any(ParameterizedTypeReference.class)))
        .thenReturn(List.of(
            new DateRateValue(LocalDate.of(2025, 2, 28), new BigDecimal("0.0036")),
            new DateRateValue(LocalDate.of(2025, 1, 31), new BigDecimal("0.0035"))));

    NavigableMap<LocalDate, BigDecimal> series = fetcher.fetch(Currency.USD);

    assertThat(series).containsExactly(
        Map.entry(LocalDate.of(2025, 1, 31), new BigDecimal("0.0035")),
        Map.entry(LocalDate.of(2025, 2, 28), new BigDecimal("0.0036")));
  }
}
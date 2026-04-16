package com.fintex.ce.adapter.webclient.boc.fetcher;

import com.fintex.ce.adapter.webclient.boc.client.BankOfCanadaProperties;
import com.fintex.ce.adapter.webclient.boc.client.BankOfCanadaProperties.CurrencyPairConfig;
import com.fintex.ce.adapter.webclient.boc.client.BankOfCanadaWebClient;
import com.fintex.ce.adapter.webclient.boc.client.FxRateSource;
import com.fintex.ce.adapter.webclient.boc.dto.BankOfCanadaFxRateResponse;
import com.fintex.ce.adapter.webclient.boc.mapper.BankOfCanadaFxRateMapper;
import com.fintex.ce.domain.model.CurrencyExchangePair;
import com.fintex.ce.domain.model.DateRange;
import com.fintex.sm.model.domain.enumeration.CurrencyType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FxRatesFetcherImplTest {

  private static final CurrencyExchangePair USD_CAD = new CurrencyExchangePair(CurrencyType.USD, CurrencyType.CAD);
  private static final CurrencyExchangePair CAD_USD = new CurrencyExchangePair(CurrencyType.CAD, CurrencyType.USD);
  private static final CurrencyExchangePair EUR_CAD = new CurrencyExchangePair(CurrencyType.EUR, CurrencyType.CAD);
  private static final DateRange DATE_RANGE = new DateRange(
      LocalDate.of(2020, 1, 1), LocalDate.of(2024, 12, 31));

  @Mock
  private BankOfCanadaWebClient client;

  @Mock
  private BankOfCanadaFxRateMapper mapper;

  private BankOfCanadaProperties properties;

  private FxRatesFetcherImpl fetcher;

  @BeforeEach
  void setUp() {
    properties = new BankOfCanadaProperties();
    fetcher = new FxRatesFetcherImpl(client, mapper, properties);
  }

  @Test
  void shouldFetchFromSingleSource() {
    configureCurrencyPair("USD_CAD",
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), "2017-01-01", null));

    var response = new BankOfCanadaFxRateResponse();
    Map<LocalDate, BigDecimal> expectedRates = Map.of(
        LocalDate.of(2024, 1, 31), new BigDecimal("1.3450"));

    when(client.get("/observations/FXUSDCAD/json?start_date=2017-01-01", BankOfCanadaFxRateResponse.class))
        .thenReturn(response);
    when(mapper.map(eq(response), eq(List.of("FXUSDCAD")), any())).thenReturn(expectedRates);

    var result = fetcher.fetch(USD_CAD, DATE_RANGE);

    assertThat(result).isEqualTo(expectedRates);
  }

  @Test
  void shouldInvertRatesWhenRequestingInversePair() {
    configureCurrencyPair("USD_CAD",
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), "2017-01-01", null));

    var response = new BankOfCanadaFxRateResponse();
    when(client.get("/observations/FXUSDCAD/json?start_date=2017-01-01", BankOfCanadaFxRateResponse.class))
        .thenReturn(response);
    when(mapper.map(eq(response), eq(List.of("FXUSDCAD")), any())).thenReturn(Map.of(
        LocalDate.of(2024, 1, 31), new BigDecimal("2.0000")));

    var result = fetcher.fetch(CAD_USD, DATE_RANGE);

    assertThat(result.get(LocalDate.of(2024, 1, 31))).isEqualByComparingTo("0.5");
  }

  @Test
  void shouldReturnEmptyMapWhenSameCurrency() {
    var result = fetcher.fetch(new CurrencyExchangePair(CurrencyType.CAD, CurrencyType.CAD), DATE_RANGE);

    assertThat(result).isEmpty();
    verifyNoInteractions(client);
  }

  @Test
  void shouldReturnEmptyMapWhenCurrencyNotConfigured() {
    var result = fetcher.fetch(EUR_CAD, DATE_RANGE);

    assertThat(result).isEmpty();
    verifyNoInteractions(client);
  }

  @Test
  void shouldMergeMultipleSources() {
    configureCurrencyPair("USD_CAD",
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), "2017-01-01", null),
        source("/observations/IEXM0101/json", List.of("IEXM0101"), null, "2016-12-31"));

    var currentResponse = new BankOfCanadaFxRateResponse();
    var legacyResponse = new BankOfCanadaFxRateResponse();

    when(client.get("/observations/FXUSDCAD/json?start_date=2017-01-01", BankOfCanadaFxRateResponse.class))
        .thenReturn(currentResponse);
    when(client.get("/observations/IEXM0101/json?end_date=2016-12-31", BankOfCanadaFxRateResponse.class))
        .thenReturn(legacyResponse);
    when(mapper.map(eq(currentResponse), eq(List.of("FXUSDCAD")), any())).thenReturn(Map.of(
        LocalDate.of(2024, 1, 31), new BigDecimal("1.3450")));
    when(mapper.map(eq(legacyResponse), eq(List.of("IEXM0101")), any())).thenReturn(Map.of(
        LocalDate.of(2016, 12, 31), new BigDecimal("1.3400")));

    var wideRange = new DateRange(LocalDate.of(2010, 1, 1), LocalDate.of(2024, 12, 31));
    var result = fetcher.fetch(USD_CAD, wideRange);

    assertThat(result).hasSize(2);
    assertThat(result).containsKey(LocalDate.of(2024, 1, 31));
    assertThat(result).containsKey(LocalDate.of(2016, 12, 31));
  }

  @Test
  void shouldSkipSourcesOutsideRequestedRange() {
    configureCurrencyPair("USD_CAD",
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), "2017-01-01", null),
        source("/observations/IEXM0101/json", List.of("IEXM0101"), null, "2016-12-31"));

    var response = new BankOfCanadaFxRateResponse();
    when(client.get("/observations/FXUSDCAD/json?start_date=2017-01-01", BankOfCanadaFxRateResponse.class))
        .thenReturn(response);
    when(mapper.map(eq(response), eq(List.of("FXUSDCAD")), any())).thenReturn(Map.of());

    fetcher.fetch(USD_CAD, DATE_RANGE);

    verify(client).get("/observations/FXUSDCAD/json?start_date=2017-01-01", BankOfCanadaFxRateResponse.class);
  }

  @Test
  void shouldPreferEarlierSourceOnDateConflict() {
    configureCurrencyPair("USD_CAD",
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), null, null),
        source("/observations/IEXM0101/json", List.of("IEXM0101"), null, null));

    var primaryResponse = new BankOfCanadaFxRateResponse();
    var fallbackResponse = new BankOfCanadaFxRateResponse();
    LocalDate overlapDate = LocalDate.of(2017, 3, 31);

    when(client.get(any(), eq(BankOfCanadaFxRateResponse.class)))
        .thenReturn(primaryResponse)
        .thenReturn(fallbackResponse);
    when(mapper.map(eq(primaryResponse), eq(List.of("FXUSDCAD")), any()))
        .thenReturn(Map.of(overlapDate, new BigDecimal("1.3450")));
    when(mapper.map(eq(fallbackResponse), eq(List.of("IEXM0101")), any()))
        .thenReturn(Map.of(overlapDate, new BigDecimal("1.9999")));

    var result = fetcher.fetch(USD_CAD, DATE_RANGE);

    assertThat(result.get(overlapDate)).isEqualByComparingTo("1.3450");
  }

  @Test
  void shouldPropagateClientException() {
    configureCurrencyPair("USD_CAD",
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), null, null));

    when(client.get(any(), eq(BankOfCanadaFxRateResponse.class)))
        .thenThrow(new RuntimeException("Connection refused"));

    assertThatThrownBy(() -> fetcher.fetch(USD_CAD, DATE_RANGE))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Connection refused");
  }

  private void configureCurrencyPair(String pair, FxRateSource... sources) {
    var config = new CurrencyPairConfig();
    config.setRateSources(List.of(sources));
    properties.getCurrencyPairs().put(pair, config);
  }

  private FxRateSource source(String path, List<String> seriesNames, String startDate, String endDate) {
    var s = new FxRateSource();
    s.setPath(path);
    s.setSeriesNames(seriesNames);
    s.setStartDate(startDate);
    s.setEndDate(endDate);
    return s;
  }
}
package com.fintex.ce.adapter.webclient.boc.fetcher;

import com.fintex.ce.adapter.webclient.boc.client.BankOfCanadaProperties;
import com.fintex.ce.adapter.webclient.boc.client.BankOfCanadaProperties.CurrencyPairConfig;
import com.fintex.ce.adapter.webclient.boc.client.BankOfCanadaWebClient;
import com.fintex.ce.adapter.webclient.boc.client.FxRateSource;
import com.fintex.ce.adapter.webclient.boc.dto.BankOfCanadaFxRateResponse;
import com.fintex.ce.adapter.webclient.boc.mapper.BankOfCanadaFxRateMapper;
import com.fintex.ce.adapter.webclient.observability.ExternalServiceObservability;
import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BocFxRatesFetcherTest {

  private static final String USD_CAD_KEY = "USD_CAD";
  private static final CurrencyExchangePair USD_CAD = new CurrencyExchangePair(Currency.USD, Currency.CAD);
  private static final CurrencyExchangePair CAD_USD = new CurrencyExchangePair(Currency.CAD, Currency.USD);
  private static final CurrencyExchangePair EUR_CAD = new CurrencyExchangePair(Currency.EUR, Currency.CAD);
  private static final DateRange DATE_RANGE = new DateRange(
      LocalDate.of(2020, 1, 1), LocalDate.of(2024, 12, 31));

  @Mock
  private BankOfCanadaWebClient client;

  @Mock
  private BankOfCanadaFxRateMapper mapper;

  @Mock
  private ExternalServiceObservability observability;

  private final BankOfCanadaProperties properties = new BankOfCanadaProperties();

  private BocFxRatesFetcher fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new BocFxRatesFetcher(client, mapper, properties, observability);
  }

  @Test
  void shouldFetchFromSingleSource() {
    configureUsdCadPair(
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), "2017-01-01", null));

    var response = new BankOfCanadaFxRateResponse();
    Map<LocalDate, BigDecimal> expectedRates = Map.of(
        LocalDate.of(2024, 1, 31), new BigDecimal("1.3450"));

    when(client.get("/observations/FXUSDCAD/json?start_date=2020-01-01&end_date=2024-12-31",
        BankOfCanadaFxRateResponse.class))
        .thenReturn(response);
    when(mapper.map(eq(response), eq(List.of("FXUSDCAD")), any())).thenReturn(expectedRates);

    var result = fetcher.fetch(USD_CAD, DATE_RANGE);

    assertThat(result).isEqualTo(expectedRates);
  }

  @Test
  void shouldInvertRatesWhenRequestingInversePair() {
    configureUsdCadPair(
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), "2017-01-01", null));

    var response = new BankOfCanadaFxRateResponse();
    when(client.get("/observations/FXUSDCAD/json?start_date=2020-01-01&end_date=2024-12-31",
        BankOfCanadaFxRateResponse.class))
        .thenReturn(response);
    when(mapper.map(eq(response), eq(List.of("FXUSDCAD")), any())).thenReturn(Map.of(
        LocalDate.of(2024, 1, 31), new BigDecimal("2.0000")));

    var result = fetcher.fetch(CAD_USD, DATE_RANGE);

    assertThat(result.get(LocalDate.of(2024, 1, 31))).isEqualByComparingTo("0.5");
  }

  @Test
  void shouldReturnEmptyMapWhenSameCurrency() {
    var result = fetcher.fetch(new CurrencyExchangePair(Currency.CAD, Currency.CAD), DATE_RANGE);

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
    configureUsdCadPair(
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), "2017-01-01", null),
        source("/observations/IEXM0101/json", List.of("IEXM0101"), null, "2016-12-31"));

    var currentResponse = new BankOfCanadaFxRateResponse();
    var legacyResponse = new BankOfCanadaFxRateResponse();

    when(client.get("/observations/FXUSDCAD/json?start_date=2017-01-01&end_date=2024-12-31",
        BankOfCanadaFxRateResponse.class))
        .thenReturn(currentResponse);
    when(client.get("/observations/IEXM0101/json?start_date=2010-01-01&end_date=2016-12-31",
        BankOfCanadaFxRateResponse.class))
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
    configureUsdCadPair(
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), "2017-01-01", null),
        source("/observations/IEXM0101/json", List.of("IEXM0101"), null, "2016-12-31"));

    var response = new BankOfCanadaFxRateResponse();
    when(client.get("/observations/FXUSDCAD/json?start_date=2020-01-01&end_date=2024-12-31",
        BankOfCanadaFxRateResponse.class))
        .thenReturn(response);
    when(mapper.map(eq(response), eq(List.of("FXUSDCAD")), any())).thenReturn(Map.of());

    fetcher.fetch(USD_CAD, DATE_RANGE);

    verify(client).get("/observations/FXUSDCAD/json?start_date=2020-01-01&end_date=2024-12-31",
        BankOfCanadaFxRateResponse.class);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("singleBoundRanges")
  void shouldFetchOnlyOverlappingSource_whenDateRangeHasSingleBound(
      String scenario,
      DateRange range,
      String expectedUrl,
      List<String> expectedSeriesNames,
      LocalDate expectedRateDate,
      String expectedRate) {
    configureUsdCadPair(
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), "2017-01-01", null),
        source("/observations/IEXM0101/json", List.of("IEXM0101"), null, "2016-12-31"));

    var response = new BankOfCanadaFxRateResponse();
    BigDecimal expectedRateValue = new BigDecimal(expectedRate);
    when(client.get(expectedUrl, BankOfCanadaFxRateResponse.class)).thenReturn(response);
    when(mapper.map(eq(response), eq(expectedSeriesNames), any()))
        .thenReturn(Map.of(expectedRateDate, expectedRateValue));

    var result = fetcher.fetch(USD_CAD, range);

    assertThat(result).containsExactly(Map.entry(expectedRateDate, expectedRateValue));
    verify(client).get(expectedUrl, BankOfCanadaFxRateResponse.class);
    verifyNoMoreInteractions(client);
  }

  @Test
  void shouldPreferEarlierSourceOnDateConflict() {
    configureUsdCadPair(
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), null, null),
        source("/observations/IEXM0101/json", List.of("IEXM0101"), null, null));

    var primaryResponse = new BankOfCanadaFxRateResponse();
    var fallbackResponse = new BankOfCanadaFxRateResponse();
    LocalDate overlapDate = LocalDate.of(2024, 3, 31);

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
  void shouldReportPairAsCanonical_whenDirectPairConfigured() {
    configureUsdCadPair(
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), null, null));

    assertThat(fetcher.canonicalDirection(USD_CAD)).isEqualTo(USD_CAD);
  }

  @Test
  void shouldReportInverseAsCanonical_whenOnlyInversePairConfigured() {
    configureUsdCadPair(
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), null, null));

    assertThat(fetcher.canonicalDirection(CAD_USD)).isEqualTo(USD_CAD);
  }

  @Test
  void shouldReportPairAsCanonical_whenNeitherDirectionConfigured() {
    assertThat(fetcher.canonicalDirection(EUR_CAD)).isEqualTo(EUR_CAD);
  }

  @Test
  void shouldPropagateClientException() {
    configureUsdCadPair(
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), null, null));

    when(client.get(any(), eq(BankOfCanadaFxRateResponse.class)))
        .thenThrow(new RuntimeException("Connection refused"));

    assertThatThrownBy(() -> fetcher.fetch(USD_CAD, DATE_RANGE))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Connection refused");
  }

  @Test
  void shouldUseRequestDatesInUrl_ratherThanSourceConfig() {
    configureUsdCadPair(
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), "2017-01-01", null));

    var narrowRange = new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 5));
    var response = new BankOfCanadaFxRateResponse();
    when(client.get("/observations/FXUSDCAD/json?start_date=2024-01-01&end_date=2024-01-05",
        BankOfCanadaFxRateResponse.class))
        .thenReturn(response);
    when(mapper.map(eq(response), eq(List.of("FXUSDCAD")), any())).thenReturn(Map.of());

    fetcher.fetch(USD_CAD, narrowRange);

    verify(client).get("/observations/FXUSDCAD/json?start_date=2024-01-01&end_date=2024-01-05",
        BankOfCanadaFxRateResponse.class);
  }

  @Test
  void shouldReturnOnlyRatesWithinRequestedRange_whenMapperReturnsWiderData() {
    configureUsdCadPair(
        source("/observations/FXUSDCAD/json", List.of("FXUSDCAD"), null, null));

    LocalDate firstRequestedDate = LocalDate.of(2024, 1, 2);
    LocalDate lastRequestedDate = LocalDate.of(2024, 1, 3);
    DateRange range = new DateRange(firstRequestedDate, lastRequestedDate);
    var response = new BankOfCanadaFxRateResponse();
    Map<LocalDate, BigDecimal> mappedRates = Map.of(
        firstRequestedDate.minusDays(1), new BigDecimal("1.3400"),
        firstRequestedDate, new BigDecimal("1.3450"),
        lastRequestedDate, new BigDecimal("1.3500"),
        lastRequestedDate.plusDays(1), new BigDecimal("1.3550"));
    when(client.get("/observations/FXUSDCAD/json?start_date=2024-01-02&end_date=2024-01-03",
        BankOfCanadaFxRateResponse.class))
        .thenReturn(response);
    when(mapper.map(eq(response), eq(List.of("FXUSDCAD")), any())).thenReturn(mappedRates);

    var result = fetcher.fetch(USD_CAD, range);

    assertThat(result).containsExactly(
        Map.entry(firstRequestedDate, new BigDecimal("1.3450")),
        Map.entry(lastRequestedDate, new BigDecimal("1.3500")));
  }

  @Test
  void shouldClampRequestDatesToSourceWindow() {
    configureUsdCadPair(
        source("/observations/IEXM0101/json", List.of("IEXM0101"), "2010-01-01", "2016-12-31"));

    var rangeBeyondSource = new DateRange(LocalDate.of(2005, 1, 1), LocalDate.of(2024, 12, 31));
    var response = new BankOfCanadaFxRateResponse();
    when(client.get("/observations/IEXM0101/json?start_date=2010-01-01&end_date=2016-12-31",
        BankOfCanadaFxRateResponse.class))
        .thenReturn(response);
    when(mapper.map(eq(response), eq(List.of("IEXM0101")), any())).thenReturn(Map.of());

    fetcher.fetch(USD_CAD, rangeBeyondSource);

    verify(client).get("/observations/IEXM0101/json?start_date=2010-01-01&end_date=2016-12-31",
        BankOfCanadaFxRateResponse.class);
  }

  private static Stream<Arguments> singleBoundRanges() {
    return Stream.of(
        Arguments.of(
            "start-only range",
            new DateRange(LocalDate.of(2024, 1, 1), null),
            "/observations/FXUSDCAD/json?start_date=2024-01-01",
            List.of("FXUSDCAD"),
            LocalDate.of(2024, 1, 2),
            "1.3450"),
        Arguments.of(
            "end-only range",
            new DateRange(null, LocalDate.of(2016, 12, 31)),
            "/observations/IEXM0101/json?end_date=2016-12-31",
            List.of("IEXM0101"),
            LocalDate.of(2016, 12, 31),
            "1.3400"));
  }

  private void configureUsdCadPair(FxRateSource... sources) {
    var config = new CurrencyPairConfig();
    config.setRateSources(List.of(sources));
    properties.getCurrencyPairs().put(USD_CAD_KEY, config);
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
package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.InterestFreq;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.GeographicExposureResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.GeographicAllocation;
import com.fintex.wm.commons.domain.allocation.GeographicAllocationValue;
import com.fintex.wm.commons.domain.allocation.GeographicAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;
import com.fintex.wm.commons.domain.allocation.RegionDatapoint;
import com.fintex.wm.commons.domain.allocation.SecurityRegion;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.financial.Geography;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.reference.CountryDatapoint;
import com.fintex.wm.commons.error.Notification;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.cash;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.etfCa;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.fundCa;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.gic;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.stockCa;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * End-to-end coverage for the consolidated {@code /geographic-exposure} endpoint. Exercises the full stack on
 * portfolios mixing mutual funds, ETFs, individual stocks, cash and GICs: the whole-security region breakdown is
 * fetched from Market Investment Catalogue and reported as given, stocks are resolved through their geography, and cash
 * and GICs drop out while the reported regions still total 100%. Request bodies are built from the same DTO classes the
 * controller deserializes, so shape changes break the test at compile time.
 */
@Tag("e2e")
class GeographicExposureE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final String ATTRIBUTES_PATH = "/api/v1/wealth/securities/attributes";
  private static final BigDecimal TOLERANCE = new BigDecimal("0.0001");
  private static final List<DataProvider> MORNINGSTAR_ONLY = List.of(DataProvider.MORNINGSTAR);

  @Override
  protected String metricPath() {
    return CalculationMetric.GEOGRAPHIC_EXPOSURE.getValue();
  }

  @Override
  protected String requestBodyForMicUnavailableScenario() {
    return writeJson(exposureCommand(
        fundCa("F00000ZJN3", 50_000),
        fundCa("F00000ZJN4", 50_000)));
  }

  /**
   * A portfolio shaped like a client's rather than the minimum the shared scenario needs: two mutual funds, an ETF, two
   * individual stocks, cash and a GIC. Every branch of the metric runs in this one request — funds and the ETF through
   * the allocation attribute, the stocks through {@code GEOGRAPHY} (one resolved by business country, one falling back
   * to the coarse security region), and cash plus the GIC excluded from the breakdown while the reported regions still
   * total 100%.
   *
   * <p>
   * Values are picked so the expected distribution is exact rather than a rounded approximation, and every attribute
   * row is denominated in CAD on purpose: the FX rate would otherwise come from the live Bank of Canada endpoint, which
   * would make the expected percentages depend on the rate of the day. Multi-currency weighting is covered by
   * {@code GeographicExposureServiceTest}, where the rate is stubbed.
   */
  @Override
  protected String requestBodyForPositiveMicScenario() {
    return writeJson(exposureCommand(
        fundCa("F00000ZJN3", 40_000),
        fundCa("F00000ZJN4", 20_000),
        etfCa("XAW", 20_000),
        stockCa("RY.TO", "TSX", 10_000),
        stockCa("AAPL", "NASDAQ", 10_000),
        cash(Currency.CAD, 20_000),
        gic(new SecurityIdentifier("GIC-RBC-2Y", FiIdentifierType.TICKER), Currency.CAD,
            BigDecimal.valueOf(10_000), BigDecimal.valueOf(730), new BigDecimal("4.75"), InterestFreq.ANNUAL,
            LocalDate.of(2024, 9, 1))));
  }

  @Override
  protected String micPositiveResponseBody() {
    return writeJson(Map.of(
        CompositeSecurityAttribute.GEOGRAPHIC_ALLOCATION,
        List.of(
            allocationRow("F00000ZJN3", FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
                regionValue(GeographicRegionType.US, "0.50"),
                regionValue(GeographicRegionType.CANADA, "0.30"),
                regionValue(GeographicRegionType.EUROPE, "0.20")),
            allocationRow("F00000ZJN4", FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
                regionValue(GeographicRegionType.CANADA, "1.00")),
            allocationRow("XAW", FiIdentifierType.TICKER, Currency.CAD,
                regionValue(GeographicRegionType.ASIA, "0.60"),
                regionValue(GeographicRegionType.EUROPE, "0.40"))),
        CompositeSecurityAttribute.GEOGRAPHY,
        List.of(
            geographyRow("F00000ZJN3", FiIdentifierType.MORNINGSTAR_ID, null, Currency.CAD),
            geographyRow("F00000ZJN4", FiIdentifierType.MORNINGSTAR_ID, null, Currency.CAD),
            geographyRow("XAW", FiIdentifierType.TICKER, null, Currency.CAD),
            businessCountryRow("RY.TO", FiIdentifierType.TICKER_MIC, Country.CANADA, Currency.CAD),
            geographyRow("AAPL", FiIdentifierType.TICKER_MIC, SecurityRegion.USA, Currency.CAD))));
  }

  /**
   * The shared positive scenario enqueues one response, which suffices for a single holding; this portfolio holds
   * seven, and how many attribute calls the fetcher batches them into is an implementation detail. Answering every
   * {@code /attributes} call from a dispatcher keeps the test about the metric rather than about the batching.
   */
  @Override
  protected void enqueueForPositiveMicScenario() {
    micMockServer.setDispatcher(attributesDispatcher(micPositiveResponseBody()));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(fundCa("F00000ZJN3", 50_000)));
    return writeJson(command);
  }

  /**
   * The full expected payload, derived by hand from the request above: the non-cash holdings total 100 000, so the two
   * funds, the ETF and the two stocks weigh 0.4 / 0.2 / 0.2 / 0.1 / 0.1. US is 0.4·0.50 + 0.1 (AAPL) = 0.30; CANADA is
   * 0.4·0.30 + 0.2·1.00 + 0.1 (RY) = 0.42; EUROPE is 0.4·0.20 + 0.2·0.40 = 0.16; ASIA is 0.2·0.60 = 0.12. The four
   * untouched regions are asserted as zeros rather than ignored, because the client donut renders them as "0%" and a
   * bucket quietly picking up weight is a real defect.
   */
  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    GeographicExposureResult result = readJson(responseBody, GeographicExposureResult.class);

    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getGeographicExposure()).containsOnlyKeys(GeographicRegionType.values());
    assertExactly(result, GeographicRegionType.US, new BigDecimal("0.30"));
    assertExactly(result, GeographicRegionType.CANADA, new BigDecimal("0.42"));
    assertExactly(result, GeographicRegionType.EUROPE, new BigDecimal("0.16"));
    assertExactly(result, GeographicRegionType.ASIA, new BigDecimal("0.12"));
    assertZero(result, GeographicRegionType.LATIN_AMERICA, GeographicRegionType.AFRICA, GeographicRegionType.OTHER,
        GeographicRegionType.UNKNOWN);
    assertThat(totalOf(result)).isEqualByComparingTo(BigDecimal.ONE);
  }

  /**
   * The one scenario whose weights do not terminate in base 10 — a fund at 2/3 and a stock at 1/3 of the non-cash value
   * — so no exact expectation can be written down and the 100% total can only hold to within rounding. The
   * mixed-portfolio shape it also exercises is covered more fully by the shared positive scenario; what is unique here
   * is that the reported regions still add up when the arithmetic cannot be exact.
   */
  @Test
  void shouldStillTotalOneHundredPercent_whenPortfolioWeightsDoNotTerminate() {
    micMockServer.setDispatcher(routingDispatcher(
        List.of(
            allocationRow("F0CAN999", FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
                regionValue(GeographicRegionType.US, "0.50"),
                regionValue(GeographicRegionType.CANADA, "0.50"))),
        List.of(
            geographyRow("F0CAN999", FiIdentifierType.MORNINGSTAR_ID, null, Currency.CAD),
            geographyRow("RY.TO", FiIdentifierType.TICKER_MIC, SecurityRegion.CANADA, Currency.CAD))));

    var response = postCalculation(writeJson(exposureCommand(
        fundCa("F0CAN999", 50_000),
        stockCa("RY.TO", "TSX", 25_000),
        cash(Currency.CAD, 25_000))));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    GeographicExposureResult result = readJson(response.responseBody(), GeographicExposureResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertCloseTo(result, GeographicRegionType.US, new BigDecimal("0.3333"));
    assertCloseTo(result, GeographicRegionType.CANADA, new BigDecimal("0.6667"));
    assertThat(totalOf(result)).isCloseTo(BigDecimal.ONE, within(TOLERANCE));
  }

  /**
   * A region bucket Market Investment Catalogue could not attribute to a known country — supranational issuers are the
   * routine case — must survive the whole pipeline in OTHER. Rolling the country datapoint up inside this service
   * instead would drop it: those entries arrive with a null country type and the mapper discards them, after which the
   * remaining regions are renormalized over the hole. That is the defect this metric's source was changed to avoid, so
   * it is pinned here.
   */
  @Test
  void shouldKeepUnattributableExposureInOther_whenMarketInvestmentCatalogueBucketedItThere() {
    micMockServer.setDispatcher(routingDispatcher(
        List.of(
            allocationRow("F0CAN999", FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
                regionValue(GeographicRegionType.CANADA, "0.70"),
                regionValue(GeographicRegionType.OTHER, "0.30", "Supranational"))),
        List.of(geographyRow("F0CAN999", FiIdentifierType.MORNINGSTAR_ID, null, Currency.CAD))));

    var response = postCalculation(writeJson(exposureCommand(fundCa("F0CAN999", 50_000))));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    GeographicExposureResult result = readJson(response.responseBody(), GeographicExposureResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertExactly(result, GeographicRegionType.CANADA, new BigDecimal("0.70"));
    assertExactly(result, GeographicRegionType.OTHER, new BigDecimal("0.30"));
    assertThat(totalOf(result)).isEqualByComparingTo(BigDecimal.ONE);
  }

  @Test
  void shouldReportUnknownAndWarn_whenMarketInvestmentCatalogueHasNoGeographicAllocationForFund() {
    micMockServer.setDispatcher(routingDispatcher(
        List.of(allocationRow("F0CAN999", FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
            regionValue(GeographicRegionType.US, "1.0"))),
        List.of(
            geographyRow("F0CAN999", FiIdentifierType.MORNINGSTAR_ID, null, Currency.CAD),
            geographyRow("F0CAN-GHOST", FiIdentifierType.MORNINGSTAR_ID, null, Currency.CAD))));

    var response = postCalculation(writeJson(exposureCommand(
        fundCa("F0CAN999", 50_000),
        fundCa("F0CAN-GHOST", 50_000))));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    GeographicExposureResult result = readJson(response.responseBody(), GeographicExposureResult.class);
    assertThat(result.getWarnings()).hasSize(1);
    Notification warning = result.getWarnings().getFirst();
    assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC);
    assertThat(warning.getMessage()).isEqualTo("Security information not found by the data source for "
        + CalculationMetric.GEOGRAPHIC_EXPOSURE.getUserFriendlyName());
    assertThat(warning.getMetadata()).containsEntry("holdingId", "MUTUAL_FUND-F0CAN-GHOST");
    assertExactly(result, GeographicRegionType.US, new BigDecimal("0.5"));
    assertExactly(result, GeographicRegionType.UNKNOWN, new BigDecimal("0.5"));
    assertThat(totalOf(result)).isEqualByComparingTo(BigDecimal.ONE);
  }

  private static PortfolioHoldingsCommand exposureCommand(PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder()
        .metric(CalculationMetric.GEOGRAPHIC_EXPOSURE)
        .holdings(List.of(holdings))
        .dataProviders(MORNINGSTAR_ONLY)
        .build();
  }

  private static Dispatcher routingDispatcher(
      List<SecurityAttributeResult<GeographicAllocationWithCurrency>> allocationRows,
      List<SecurityAttributeResult<Geography>> geographyRows) {
    return attributesDispatcher(writeJson(Map.of(
        CompositeSecurityAttribute.GEOGRAPHIC_ALLOCATION, allocationRows,
        CompositeSecurityAttribute.GEOGRAPHY, geographyRows)));
  }

  private static Dispatcher attributesDispatcher(String compositeBody) {
    return new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        String path = request.getPath();
        if (path != null && path.contains(ATTRIBUTES_PATH)) {
          return new MockResponse()
              .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .setBody(compositeBody);
        }
        return new MockResponse().setResponseCode(404);
      }
    };
  }

  /**
   * The whole-security attribute row as Market Investment Catalogue serves it: the region breakdown paired with the
   * currency the security's values are denominated in, which is why this metric needs no second attribute to weight
   * across currencies.
   */
  private static SecurityAttributeResult<GeographicAllocationWithCurrency> allocationRow(String id,
      FiIdentifierType idType, Currency currency, GeographicAllocationValue... values) {
    GeographicAllocation allocation = new GeographicAllocation();
    allocation.setAllocations(new ArrayList<>(List.of(values)));
    allocation.setDataProviders(MORNINGSTAR_ONLY);

    CurrencyDatapoint currencyDp = new CurrencyDatapoint();
    currencyDp.setValue(currency);

    GeographicAllocationWithCurrency row = GeographicAllocationWithCurrency.builder()
        .geographicAllocation(allocation)
        .currency(currencyDp)
        .build();
    row.setDataProviders(MORNINGSTAR_ONLY);
    return attributeResult(id, idType, row);
  }

  private static GeographicAllocationValue regionValue(GeographicRegionType region, String value,
      String... originalTypeNames) {
    return new GeographicAllocationValue(region, new BigDecimal(value), new TreeSet<>(List.of(originalTypeNames)));
  }

  /**
   * A geography row carrying the business country — the stock branch's primary resolution path, which maps onto the
   * full eight-value region scale. The {@link #geographyRow} variant carries only the coarse {@link SecurityRegion}, so
   * the two together exercise both the primary path and the fallback in one scenario.
   */
  private static SecurityAttributeResult<Geography> businessCountryRow(String id, FiIdentifierType idType,
      Country businessCountry, Currency currency) {
    SecurityAttributeResult<Geography> row = geographyRow(id, idType, null, currency);
    row.getData().setBusinessCountry(new CountryDatapoint(businessCountry));
    return row;
  }

  private static SecurityAttributeResult<Geography> geographyRow(String id, FiIdentifierType idType,
      SecurityRegion region, Currency currency) {
    Geography geography = new Geography();
    if (region != null) {
      RegionDatapoint regionDp = new RegionDatapoint();
      regionDp.setValue(region);
      geography.setRegion(regionDp);
    }
    if (currency != null) {
      CurrencyDatapoint currencyDp = new CurrencyDatapoint();
      currencyDp.setValue(currency);
      geography.setCurrency(currencyDp);
    }
    geography.setDataProviders(MORNINGSTAR_ONLY);
    return attributeResult(id, idType, geography);
  }

  private static <T> SecurityAttributeResult<T> attributeResult(String id, FiIdentifierType idType, T data) {
    SecurityIdentifier identifier = new SecurityIdentifier();
    identifier.setId(id);
    identifier.setIdType(idType);
    SecurityAttributeResult<T> result = new SecurityAttributeResult<>();
    result.setIdentifier(identifier);
    result.setData(data);
    return result;
  }

  private static void assertZero(GeographicExposureResult result, GeographicRegionType... regions) {
    for (GeographicRegionType region : regions) {
      assertExactly(result, region, BigDecimal.ZERO);
    }
  }

  /**
   * For scenarios whose weights terminate in base 10, which is all of them bar one: the reported value must be the
   * expected one, not merely near it. A tolerance there would accept a rounding regression at the fourth decimal — and
   * the client renders two — and, applied to an expected zero, would accept a bucket that quietly picked up 0.009% of
   * the portfolio.
   */
  private static void assertExactly(GeographicExposureResult result, GeographicRegionType region,
      BigDecimal expected) {
    BigDecimal actual = result.getGeographicExposure().get(region);
    assertThat(actual).as("region %s", region).isNotNull();
    assertThat(actual).as("region %s", region).isEqualByComparingTo(expected);
  }

  /**
   * Only for weights that do not terminate, where an exact expectation cannot be written down.
   */
  private static void assertCloseTo(GeographicExposureResult result, GeographicRegionType region,
      BigDecimal expected) {
    BigDecimal actual = result.getGeographicExposure().get(region);
    assertThat(actual).as("region %s", region).isNotNull();
    assertThat(actual).as("region %s", region).isCloseTo(expected, within(TOLERANCE));
  }

  private static BigDecimal totalOf(GeographicExposureResult result) {
    return result.getGeographicExposure().values().stream()
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}

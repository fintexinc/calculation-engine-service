package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.EquitySectorResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocation;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationTypeValue;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.EquitySectorDatapoint;
import com.fintex.wm.commons.domain.allocation.EquitySectorWithCurrency;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.error.ErrorResponse;
import com.fintex.wm.commons.error.Notification;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

@Tag("e2e")
class EquitySectorE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final SecurityIdentifier FIRST_ETF_IDENTIFIER = new SecurityIdentifier("F00000ZJN3",
      FiIdentifierType.MORNINGSTAR_ID);
  private static final SecurityIdentifier SECOND_ETF_IDENTIFIER = new SecurityIdentifier("F00000ZJN4",
      FiIdentifierType.MORNINGSTAR_ID);
  private static final SecurityIdentifier THIRD_ETF_IDENTIFIER = new SecurityIdentifier("F00000ZJN5",
      FiIdentifierType.MORNINGSTAR_ID);
  private static final SecurityIdentifier FOURTH_ETF_IDENTIFIER = new SecurityIdentifier("F00000ZJN6",
      FiIdentifierType.MORNINGSTAR_ID);

  private static final SecurityIdentifier STOCK_IDENTIFIER = new SecurityIdentifier("RY.TO",
      FiIdentifierType.TICKER);

  private static final String ATTRIBUTES_PATH = "/api/v1/wealth/securities/attributes";

  @Override
  protected String metricPath() {
    return CalculationMetric.EQUITY_SECTOR.getValue();
  }

  @Override
  protected String requestBodyForMicUnavailableScenario() {
    return writeJson(equitySectorCommand(CalculationMetric.EQUITY_SECTOR));
  }

  @Override
  protected String requestBodyForPositiveMicScenario() {
    return writeJson(equitySectorCommand(CalculationMetric.EQUITY_SECTOR));
  }

  /**
   * Two attributes, because the metric asks for both: funds and ETFs answer with a sector distribution, individual
   * companies with the single sector they belong to. This portfolio is ETFs only, so the scalar attribute comes back
   * empty — the stock path has its own scenario below.
   */
  @Override
  protected String micPositiveResponseBody() {
    return writeJson(Map.of(
        CompositeSecurityAttribute.EQUITY_SECTOR_ALLOCATION, List.of(
            sectorAllocationRow(FIRST_ETF_IDENTIFIER, "0.80", "0.00"),
            sectorAllocationRow(SECOND_ETF_IDENTIFIER, "0.40", "0.40"),
            sectorAllocationRow(THIRD_ETF_IDENTIFIER, "0.00", "0.80"),
            sectorAllocationRow(FOURTH_ETF_IDENTIFIER, "0.10", "0.70")),
        CompositeSecurityAttribute.EQUITY_SECTOR, List.of()));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    return writeJson(equitySectorCommand(CalculationMetric.ASSET_ALLOCATIONS));
  }

  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    EquitySectorResult result = readJson(responseBody, EquitySectorResult.class);

    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getEquitySector().get(EquitySectorAllocationType.TECHNOLOGY)).isEqualByComparingTo("0.25");
    assertThat(result.getEquitySector().get(EquitySectorAllocationType.FINANCIAL_SERVICES))
        .isEqualByComparingTo("0.75");
    assertEveryOtherBucketIsZeroAndTheTotalIsOne(result,
        EquitySectorAllocationType.TECHNOLOGY,
        EquitySectorAllocationType.FINANCIAL_SERVICES);
  }

  /**
   * The buckets the client donut renders as "0%" are asserted rather than ignored, because a bucket quietly picking up
   * weight is a real defect, and so is a breakdown that no longer totals the whole portfolio.
   */
  private static void assertEveryOtherBucketIsZeroAndTheTotalIsOne(EquitySectorResult result,
      EquitySectorAllocationType... populated) {
    Set<EquitySectorAllocationType> expectedNonZero = Set.of(populated);
    assertThat(result.getEquitySector()).hasSize(EquitySectorAllocationType.values().length);
    assertThat(result.getEquitySector().entrySet().stream()
        .filter(entry -> !expectedNonZero.contains(entry.getKey()))
        .allMatch(entry -> entry.getValue().compareTo(ZERO) == 0)).isTrue();
    BigDecimal totalExposure = result.getEquitySector().values().stream()
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    assertThat(totalExposure).isEqualByComparingTo(BigDecimal.ONE);
  }

  @Test
  void shouldReturnBadRequest_whenTickerMicHoldingMissingExchangeId() {
    int micRequestsBefore = micMockServer.getRequestCount();

    var response = postCalculation(writeJson(tickerMicWithoutExchangeCommand()));

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());

    ErrorResponse errorResponse = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(errorResponse.getNotifications()).hasSize(1);
    Notification notification = errorResponse.getNotifications().getFirst();
    assertThat(notification.getCode()).isEqualTo(ErrorCode.FIELD_NOT_BLANK.getCode());
    assertThat(notification.getFieldName()).isEqualTo("securityIdentifier.exchangeId");
    assertThat(notification.getMessage()).isEqualTo("Security Identifier Exchange ID must not be blank");

    // The malformed body must be rejected locally before any Market Investment Catalogue call is made.
    assertThat(micMockServer.getRequestCount()).isEqualTo(micRequestsBefore);
  }

  /**
   * TMI-475 end to end: a stock reaches its sector through the scalar {@code EQUITY_SECTOR} attribute, and the ETF
   * beside it through the distribution, so a mixed portfolio buckets both. The stock also gets an
   * {@code EQUITY_SECTOR_ALLOCATION} row carrying its currency and no allocations, which is what Market Investment
   * Catalogue actually answers — that attribute is served for any security declaring one of its columns, and every
   * security declares {@code currency}. Treating that row as data would bucket the stock as UNKNOWN, so the empty
   * distribution must not displace the sector.
   *
   * <p>
   * The ETF's distribution already sums to 1 and the two holdings are equal in value, so the expected split is exact:
   * technology 0.25, financial services 0.25, energy 0.50.
   */
  @Test
  void shouldBucketTheStockByItsOwnSector_whenPortfolioMixesAnEtfAndAStock() {
    micMockServer.setDispatcher(attributesDispatcher(writeJson(Map.of(
        CompositeSecurityAttribute.EQUITY_SECTOR_ALLOCATION, List.of(
            sectorAllocationRow(FIRST_ETF_IDENTIFIER, "0.50", "0.50"),
            currencyOnlySectorAllocationRow(STOCK_IDENTIFIER)),
        CompositeSecurityAttribute.EQUITY_SECTOR, List.of(
            sectorRow(STOCK_IDENTIFIER, EquitySectorAllocationType.ENERGY))))));

    var response = postCalculation(writeJson(etfAndStockCommand()));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    EquitySectorResult result = readJson(response.responseBody(), EquitySectorResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getEquitySector().get(EquitySectorAllocationType.TECHNOLOGY)).isEqualByComparingTo("0.25");
    assertThat(result.getEquitySector().get(EquitySectorAllocationType.FINANCIAL_SERVICES))
        .isEqualByComparingTo("0.25");
    assertThat(result.getEquitySector().get(EquitySectorAllocationType.ENERGY)).isEqualByComparingTo("0.50");
    assertEveryOtherBucketIsZeroAndTheTotalIsOne(result,
        EquitySectorAllocationType.TECHNOLOGY,
        EquitySectorAllocationType.FINANCIAL_SERVICES,
        EquitySectorAllocationType.ENERGY);
  }

  private static PortfolioHoldingsCommand etfAndStockCommand() {
    return PortfolioHoldingsCommand.builder()
        .metric(CalculationMetric.EQUITY_SECTOR)
        .dataProviders(List.of(DataProvider.MORNINGSTAR))
        .holdings(List.of(
            etfHolding(50_000, FIRST_ETF_IDENTIFIER),
            new PortfolioHolding(BigDecimal.valueOf(50_000), FinancialInstrumentType.STOCK, Country.CANADA,
                STOCK_IDENTIFIER)))
        .build();
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

  private static SecurityAttributeResult<EquitySectorWithCurrency> sectorRow(SecurityIdentifier identifier,
      EquitySectorAllocationType sector) {
    EquitySectorDatapoint datapoint = new EquitySectorDatapoint();
    datapoint.setEquitySector(sector);
    datapoint.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    EquitySectorWithCurrency response = new EquitySectorWithCurrency();
    response.setSector(datapoint);
    response.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return securityAttributeResult(identifier, response);
  }

  private static SecurityAttributeResult<EquitySectorAllocationWithCurrency> currencyOnlySectorAllocationRow(
      SecurityIdentifier identifier) {
    EquitySectorAllocationWithCurrency response = new EquitySectorAllocationWithCurrency();
    response.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return securityAttributeResult(identifier, response);
  }

  private static PortfolioHoldingsCommand tickerMicWithoutExchangeCommand() {
    PortfolioHolding stock = new PortfolioHolding(
        BigDecimal.valueOf(50_000), FinancialInstrumentType.STOCK, Country.CANADA,
        EquitySecurityIdentifier.builder()
            .id("CNQ")
            .idType(FiIdentifierType.TICKER_MIC)
            .build());
    return PortfolioHoldingsCommand.builder()
        .metric(CalculationMetric.EQUITY_SECTOR)
        .dataProviders(List.of(DataProvider.MORNINGSTAR))
        .holdings(List.of(stock))
        .build();
  }

  private static PortfolioHoldingsCommand equitySectorCommand(CalculationMetric metric) {
    return PortfolioHoldingsCommand.builder()
        .metric(metric)
        .dataProviders(List.of(DataProvider.MORNINGSTAR))
        .holdings(List.of(
            etfHolding(10_000, FIRST_ETF_IDENTIFIER),
            etfHolding(20_000, SECOND_ETF_IDENTIFIER),
            etfHolding(30_000, THIRD_ETF_IDENTIFIER),
            etfHolding(40_000, FOURTH_ETF_IDENTIFIER)))
        .build();
  }

  private static PortfolioHolding etfHolding(long value, SecurityIdentifier identifier) {
    return new PortfolioHolding(BigDecimal.valueOf(value), FinancialInstrumentType.ETF, Country.CANADA, identifier);
  }

  private static SecurityAttributeResult<EquitySectorAllocationWithCurrency> sectorAllocationRow(
      SecurityIdentifier identifier, String technology, String financialServices) {
    EquitySectorAllocation allocation = new EquitySectorAllocation();
    allocation.setAllocations(List.of(
        sectorAllocation(EquitySectorAllocationType.TECHNOLOGY, technology),
        sectorAllocation(EquitySectorAllocationType.FINANCIAL_SERVICES, financialServices)));
    allocation.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    EquitySectorAllocationWithCurrency response = new EquitySectorAllocationWithCurrency();
    response.setEquitySectorAllocation(allocation);
    response.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return securityAttributeResult(identifier, response);
  }

  private static EquitySectorAllocationTypeValue sectorAllocation(EquitySectorAllocationType type, String value) {
    EquitySectorAllocationTypeValue allocation = new EquitySectorAllocationTypeValue();
    allocation.setType(type);
    allocation.setValue(new BigDecimal(value));
    return allocation;
  }
}

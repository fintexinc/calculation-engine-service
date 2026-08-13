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
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.error.ErrorResponse;
import com.fintex.wm.commons.error.Notification;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static com.fintex.ce.e2e.PortfolioHoldingBuildHelper.holdingOfCountry;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

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

  @Override
  protected String metricPath() {
    return CalculationMetric.EQUITY_SECTOR.getValue();
  }

  @Override
  protected String requestBodyForSmsUnavailableScenario() {
    return writeJson(equitySectorCommand(CalculationMetric.EQUITY_SECTOR));
  }

  @Override
  protected String requestBodyForPositiveSmsScenario() {
    return writeJson(equitySectorCommand(CalculationMetric.EQUITY_SECTOR));
  }

  @Override
  protected String smsPositiveResponseBody() {
    return writeJson(List.of(
        sectorAllocationRow(FIRST_ETF_IDENTIFIER, "0.80", "0.00"),
        sectorAllocationRow(SECOND_ETF_IDENTIFIER, "0.40", "0.40"),
        sectorAllocationRow(THIRD_ETF_IDENTIFIER, "0.00", "0.80"),
        sectorAllocationRow(FOURTH_ETF_IDENTIFIER, "0.10", "0.70")));
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
    assertThat(result.getEquitySector()).hasSize(EquitySectorAllocationType.values().length);
    assertThat(result.getEquitySector().entrySet().stream()
        .filter(entry -> entry.getKey() != EquitySectorAllocationType.TECHNOLOGY
            && entry.getKey() != EquitySectorAllocationType.FINANCIAL_SERVICES)
        .allMatch(entry -> entry.getValue().compareTo(ZERO) == 0)).isTrue();
    BigDecimal totalExposure = result.getEquitySector().values().stream()
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    assertThat(totalExposure).isEqualByComparingTo(BigDecimal.ONE);
  }

  @Test
  void shouldReturnBadRequest_whenTickerMicHoldingMissingExchangeId() {
    int smsRequestsBefore = smsMockServer.getRequestCount();

    var response = postCalculation(writeJson(tickerMicWithoutExchangeCommand()));

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());

    ErrorResponse errorResponse = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(errorResponse.getNotifications()).hasSize(1);
    Notification notification = errorResponse.getNotifications().getFirst();
    assertThat(notification.getCode()).isEqualTo(ErrorCode.FIELD_NOT_BLANK.getCode());
    assertThat(notification.getFieldName()).isEqualTo("securityIdentifier.exchangeId");
    assertThat(notification.getMessage()).isEqualTo("Security Identifier Exchange ID must not be blank");

    // The malformed body must be rejected locally before any Security Master call is made.
    assertThat(smsMockServer.getRequestCount()).isEqualTo(smsRequestsBefore);
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
            holdingOfCountry(FIRST_ETF_IDENTIFIER, FinancialInstrumentType.ETF, Country.CANADA, BigDecimal.valueOf(
                10_000)),
            holdingOfCountry(SECOND_ETF_IDENTIFIER, FinancialInstrumentType.ETF, Country.CANADA, BigDecimal.valueOf(
                20_000)),
            holdingOfCountry(THIRD_ETF_IDENTIFIER, FinancialInstrumentType.ETF, Country.CANADA, BigDecimal.valueOf(
                30_000)),
            holdingOfCountry(FOURTH_ETF_IDENTIFIER, FinancialInstrumentType.ETF, Country.CANADA,
                BigDecimal.valueOf(40_000))))
        .build();
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

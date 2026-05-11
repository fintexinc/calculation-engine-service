package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.InterestFreq;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.KeyValueResult;
import com.fintex.ce.model.domain.result.returns.Growth10KResult;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.performance.MonthlyReturns;
import com.fintex.wm.commons.domain.value.DateBigDecimalValue;

import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.QueueDispatcher;

/**
 * Shared e2e infrastructure for the {@code growth-of-10k} metric. Named {@code AbstractGrowthOf10kE2ETest} because
 * {@code GrowthOf10KE2ETest} would collide on case-insensitive filesystems with {@link GrowthOf10kE2ETest}.
 */
abstract class AbstractGrowthOf10kE2ETest extends AbstractPortfolioCalculationE2ETest {

  protected static final SecurityIdentifier XBAL = new SecurityIdentifier("XBAL", FiIdentifierType.TICKER);
  protected static final SecurityIdentifier VCNS = new SecurityIdentifier("VCNS", FiIdentifierType.TICKER);
  protected static final SecurityIdentifier VTI = new SecurityIdentifier("VTI", FiIdentifierType.TICKER);
  protected static final SecurityIdentifier SPY = new SecurityIdentifier("SPY", FiIdentifierType.TICKER);
  protected static final SecurityIdentifier F0CAN999 = new SecurityIdentifier("F0CAN999",
      FiIdentifierType.MORNINGSTAR_ID);
  protected static final SecurityIdentifier CCM4752 = new SecurityIdentifier("CCM4752", FiIdentifierType.FUNDSERV);
  protected static final EquitySecurityIdentifier RY_TO = EquitySecurityIdentifier.builder()
      .id("RY.TO")
      .idType(FiIdentifierType.TICKER_MIC)
      .exchangeId("TSX")
      .build();

  @BeforeEach
  void resetSmsMockServerQueue() {
    smsMockServer.setDispatcher(new QueueDispatcher());
  }

  @Override
  protected String metricPath() {
    return CalculationMetric.GROWTH_OF_10K.getValue();
  }

  @Override
  protected String requestBodyForSmsUnavailableScenario() {
    return writeJson(commandFor(Currency.CAD, List.of(
        etfCanada(XBAL, "45234.67"),
        etfCanada(VCNS, "18765.43"))));
  }

  @Override
  protected String requestBodyForPositiveSmsScenario() {
    return writeJson(richPortfolioCommand());
  }

  @Override
  protected String smsPositiveResponseBody() {
    return writeJson(List.of(
        securityAttributeResult(XBAL, twoMonthReturns("5.0", "-2.0")),
        securityAttributeResult(VCNS, twoMonthReturns("3.0", "1.0")),
        securityAttributeResult(F0CAN999, twoMonthReturns("4.0", "0.5")),
        securityAttributeResult(CCM4752, twoMonthReturns("2.0", "-1.0")),
        securityAttributeResult(RY_TO, twoMonthReturns("6.0", "-3.0"))));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    ReturnCommand command = richPortfolioCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    return writeJson(command);
  }

  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    Growth10KResult result = readJson(responseBody, Growth10KResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getPerformanceStartDate()).isEqualTo(LocalDate.of(2023, 12, 31));
    assertThat(result.getPerformanceEndDate()).isEqualTo(LocalDate.of(2024, 2, 29));
    assertThat(result.getGrowth10k()).hasSize(3);
    assertGrowthPoint(result.getGrowth10k().get(0), "2023-12-31", "10000");
    assertGrowthPoint(result.getGrowth10k().get(1), "2024-01-31", "10358.0242797949");
    assertGrowthPoint(result.getGrowth10k().get(2), "2024-02-29", "10293.6308701015");
  }

  protected static ReturnCommand richPortfolioCommand() {
    return commandFor(Currency.CAD, List.of(
        etfCanada(XBAL, "45234.67"),
        etfCanada(VCNS, "18765.43"),
        stockCanada(RY_TO, "9234.12"),
        fund(F0CAN999, FinancialInstrumentType.MUTUAL_FUND_CANADA, "15678.90"),
        fundServ(CCM4752, "11234.56"),
        gic(Currency.CAD, "25000.00", "365", "12.0"),
        cash(Currency.CAD, "10000.00")));
  }

  protected static void assertGrowthPoint(KeyValueResult<?> point, String expectedDate, String expectedValue) {
    assertThat(point.key()).isEqualTo(expectedDate);
    assertThat(point.value()).isEqualByComparingTo(new BigDecimal(expectedValue));
  }

  protected static ReturnCommand commandFor(Currency currency, List<PortfolioHolding> holdings) {
    ReturnCommand command = new ReturnCommand();
    command.setMetric(CalculationMetric.GROWTH_OF_10K);
    command.setCurrency(currency);
    command.setHoldings(holdings);
    return command;
  }

  protected static PortfolioHolding etfCanada(SecurityIdentifier securityIdentifier, String value) {
    return new PortfolioHolding(
        new BigDecimal(value),
        FinancialInstrumentType.ETF_CANADA,
        securityIdentifier);
  }

  protected static PortfolioHolding usEtf(SecurityIdentifier securityIdentifier, String value) {
    return new PortfolioHolding(
        new BigDecimal(value),
        FinancialInstrumentType.ETF_US,
        securityIdentifier);
  }

  protected static PortfolioHolding stockCanada(EquitySecurityIdentifier securityIdentifier, String value) {
    return new PortfolioHolding(
        new BigDecimal(value),
        FinancialInstrumentType.STOCK_CANADA,
        securityIdentifier);
  }

  protected static PortfolioHolding fund(SecurityIdentifier morningstarId, FinancialInstrumentType type, String value) {
    return new PortfolioHolding(new BigDecimal(value), type, morningstarId);
  }

  protected static PortfolioHolding fundServ(SecurityIdentifier fundservId, String value) {
    return new PortfolioHolding(
        new BigDecimal(value),
        FinancialInstrumentType.MUTUAL_FUND_CANADA,
        fundservId);
  }

  protected static CashHolding cash(Currency currency, String value) {
    return CashHolding.builder()
        .value(new BigDecimal(value))
        .holdingType(FinancialInstrumentType.CASH)
        .currency(currency)
        .build();
  }

  protected static GicHolding gic(Currency currency, String value, String termDays, String clientIntRatePercent) {
    return GicHolding.builder()
        .value(new BigDecimal(value))
        .holdingType(FinancialInstrumentType.GIC)
        .currency(currency)
        .investmentDate(LocalDate.of(2024, 1, 1))
        .clientIntRate(new BigDecimal(clientIntRatePercent))
        .interestFreq(InterestFreq.MONTHLY)
        .term(new BigDecimal(termDays))
        .build();
  }

  private static MonthlyReturns twoMonthReturns(String janPercent, String febPercent) {
    return monthlyReturns(
        returns("2024-01-31", janPercent, "2024-02-29", febPercent),
        DataProvider.MORNINGSTAR,
        "2024-02-29T00:00:00");
  }

  protected static MonthlyReturns monthlyReturns(List<DateBigDecimalValue> returns, DataProvider provider,
      String asOf) {
    MonthlyReturns monthlyReturns = new MonthlyReturns();
    monthlyReturns.setReturns(returns);
    monthlyReturns.setDataProviders(provider == null ? null : List.of(provider));
    monthlyReturns.setAsOfDate(LocalDateTime.parse(asOf));
    return monthlyReturns;
  }

  protected static List<DateBigDecimalValue> returns(String... dateValuePairs) {
    if (dateValuePairs.length % 2 != 0) {
      throw new IllegalArgumentException("expected even number of strings: date,value pairs");
    }
    var list = new ArrayList<DateBigDecimalValue>(dateValuePairs.length / 2);
    for (int i = 0; i < dateValuePairs.length; i += 2) {
      list.add(new DateBigDecimalValue(dateValuePairs[i], new BigDecimal(dateValuePairs[i + 1])));
    }
    return list;
  }
}

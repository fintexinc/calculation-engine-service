package ca.tangerine.pce.e2e;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.enumeration.InterestFreq;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.KeyValueResult;
import ca.tangerine.pce.model.domain.result.returns.Growth10KResult;
import ca.tangerine.pce.model.dto.command.ReturnCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.performance.MonthlyReturns;
import ca.tangerine.wm.commons.domain.rates.DateRateValue;
import ca.tangerine.wm.commons.error.Notification;
import ca.tangerine.wm.commons.error.Severity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.cash;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.gic;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shared e2e infrastructure for the {@code growth-of-10k} metric. Named {@code AbstractGrowthOf10kE2ETest} because
 * {@code GrowthOf10KE2ETest} would collide on case-insensitive filesystems with {@link GrowthOf10kE2ETest}. Common
 * {@link ReturnCommand} fixtures (holding factories, monthly-returns builders, identifiers) live in
 * {@link AbstractReturnCommandE2ETest}.
 */
abstract class AbstractGrowthOf10kE2ETest extends AbstractReturnCommandE2ETest {

  @Override
  protected String metricPath() {
    return CalculationMetric.GROWTH_OF_10K.getValue();
  }

  @Override
  protected String requestBodyForMicUnavailableScenario() {
    return writeJson(commandFor(Currency.CAD, List.of(
        holding(XBAL, FinancialInstrumentType.ETF, Country.CANADA, "45234.67"),
        holding(VCNS, FinancialInstrumentType.ETF, Country.CANADA, "18765.43"))));
  }

  @Override
  protected String requestBodyForPositiveMicScenario() {
    ReturnCommand command = richPortfolioCommand();
    command.setCustomPed(LocalDate.of(2024, 12, 31));
    return writeJson(command);
  }

  @Override
  protected void enqueueForPositiveMicScenario() {
    enqueueMicMockResponse(micPositiveResponseBody());
    enqueueMicMockResponse(writeJson(cadTreasuryRates()));
  }

  @Override
  protected String micPositiveResponseBody() {
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
    assertThat(result.getWarnings()).extracting(Notification::getCode)
        .containsExactly(ErrorCode.CPED_AFTER_PORTFOLIO_PED.getCode());
    assertThat(result.getWarnings()).extracting(Notification::getSeverity)
        .containsExactly(Severity.WARNING);
    assertThat(result.getPerformanceStartDate()).isEqualTo(LocalDate.of(2023, 12, 31));
    assertThat(result.getPerformanceEndDate()).isEqualTo(LocalDate.of(2024, 2, 29));
    assertThat(result.getGrowth10k()).hasSize(3);
    assertGrowthPoint(result.getGrowth10k().get(0), "2023-12-31", "10000");
    assertGrowthPoint(result.getGrowth10k().get(1), "2024-01-31", "10338.9322554409");
    assertGrowthPoint(result.getGrowth10k().get(2), "2024-02-29", "10294.7136234902");
    assertThat(result.getComparison()).isNull();
    assertThat(responseBody).doesNotContain("\"comparison\"");
  }

  protected static ReturnCommand richPortfolioCommand() {
    return commandFor(Currency.CAD, List.of(
        holding(XBAL, FinancialInstrumentType.ETF, Country.CANADA, "45234.67"),
        holding(VCNS, FinancialInstrumentType.ETF, Country.CANADA, "18765.43"),
        holding(RY_TO, FinancialInstrumentType.STOCK, Country.CANADA, "9234.12"),
        holding(F0CAN999, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "15678.90"),
        holding(CCM4752, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "11234.56"),
        gic(null, Currency.CAD, new BigDecimal("25000.00"), new BigDecimal("365"), new BigDecimal("12.0"),
            InterestFreq.MONTHLY, LocalDate.of(2024, 1, 1)),
        cash(Currency.CAD, "10000.00")));
  }

  protected static ReturnCommand commandFor(Currency currency, List<PortfolioHolding> holdings) {
    return commandFor(CalculationMetric.GROWTH_OF_10K, currency, holdings);
  }

  protected static void assertGrowthPoint(KeyValueResult<?> point, String expectedDate, String expectedValue) {
    assertThat(point.key()).isEqualTo(LocalDate.parse(expectedDate));
    assertThat(point.value()).isEqualByComparingTo(new BigDecimal(expectedValue));
  }

  protected static MonthlyReturns twoMonthReturns(String janPercent, String febPercent) {
    return monthlyReturns(
        returns("2024-01-31", janPercent, "2024-02-29", febPercent),
        DataProvider.MORNINGSTAR,
        "2024-02-29T00:00:00");
  }

  private static List<DateRateValue> cadTreasuryRates() {
    return List.of(
        new DateRateValue(LocalDate.of(2024, 1, 31), BigDecimal.ONE),
        new DateRateValue(LocalDate.of(2024, 2, 29), BigDecimal.valueOf(2)));
  }
}

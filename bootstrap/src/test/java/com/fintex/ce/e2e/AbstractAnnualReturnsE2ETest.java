package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.KeyValueResult;
import com.fintex.ce.model.domain.result.returns.AnnualReturnResult;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.performance.MonthlyReturns;
import com.fintex.wm.commons.domain.value.DateBigDecimalValue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shared e2e infrastructure for the {@code annual-returns} metric. Common {@link ReturnCommand} fixtures live in
 * {@link AbstractReturnCommandE2ETest}. Annual-returns requires a full Jan-Dec calendar year of monthly returns per
 * holding, so the positive SMS response supplies all twelve 2024 month-ends (see {@link #fullYear2024Returns()}).
 *
 * <p>
 * Every holding in the positive scenario is given the <em>same</em> monthly-return series, so the value-weighted
 * portfolio return each month equals that series regardless of holding weights — the expected annual return is then
 * simply {@code product(1 + monthlyReturn) - 1} and does not depend on the individual holding values.
 * </p>
 */
abstract class AbstractAnnualReturnsE2ETest extends AbstractReturnCommandE2ETest {

  private static final String[] MONTH_ENDS_2024 = {
      "2024-01-31", "2024-02-29", "2024-03-31", "2024-04-30", "2024-05-31", "2024-06-30",
      "2024-07-31", "2024-08-31", "2024-09-30", "2024-10-31", "2024-11-30", "2024-12-31"};

  // Realistic 12-month path in percentage points, including down months, shared by every holding.
  private static final String[] MONTHLY_PERCENTS_2024 = {
      "1.0", "-0.7", "0.9", "-1.6", "1.4", "-0.9", "0.3", "0.8", "-1.2", "1.1", "-0.5", "0.6"};

  @Override
  protected String metricPath() {
    return CalculationMetric.ANNUAL_RETURNS.getValue();
  }

  @Override
  protected String requestBodyForSmsUnavailableScenario() {
    return writeJson(commandFor(Currency.CAD, List.of(
        etfCanada(XBAL, "45234.67"),
        etfCanada(VCNS, "18765.43"))));
  }

  @Override
  protected String requestBodyForPositiveSmsScenario() {
    return writeJson(annualPortfolioCommand());
  }

  @Override
  protected String smsPositiveResponseBody() {
    return writeJson(List.of(
        securityAttributeResult(XBAL, fullYear2024Returns()),
        securityAttributeResult(VCNS, fullYear2024Returns()),
        securityAttributeResult(F0CAN999, fullYear2024Returns()),
        securityAttributeResult(CCM4752, fullYear2024Returns()),
        securityAttributeResult(RY_TO, fullYear2024Returns())));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    ReturnCommand command = annualPortfolioCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    return writeJson(command);
  }

  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    AnnualReturnResult<?> result = readJson(responseBody, AnnualReturnResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getPerformanceStartDate()).isEqualTo(LocalDate.of(2024, 1, 31));
    assertThat(result.getPerformanceEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    assertThat(result.getAnnualReturns()).hasSize(1);
    KeyValueResult<?> entry = result.getAnnualReturns().getFirst();
    assertThat(entry.key()).isEqualTo(2024);
    assertThat(entry.value()).isEqualByComparingTo(new BigDecimal("0.0114842466"));
  }

  protected static ReturnCommand commandFor(Currency currency, List<PortfolioHolding> holdings) {
    return commandFor(CalculationMetric.ANNUAL_RETURNS, currency, holdings);
  }

  protected static ReturnCommand annualPortfolioCommand() {
    return commandFor(Currency.CAD, List.of(
        etfCanada(XBAL, "45234.67"),
        etfCanada(VCNS, "18765.43"),
        stockCanada(RY_TO, "9234.12"),
        fund(F0CAN999, FinancialInstrumentType.MUTUAL_FUND_CANADA, "15678.90"),
        fundServ(CCM4752, "11234.56")));
  }

  protected static MonthlyReturns fullYear2024Returns() {
    List<DateBigDecimalValue> monthly = IntStream.range(0, MONTH_ENDS_2024.length)
        .mapToObj(i -> new DateBigDecimalValue(MONTH_ENDS_2024[i], new BigDecimal(MONTHLY_PERCENTS_2024[i])))
        .toList();
    return monthlyReturns(monthly, DataProvider.MORNINGSTAR, "2024-12-31T00:00:00");
  }

  /**
   * Builds a monthly-returns series over the given month-end keys with a constant 1.0% each. The value is irrelevant
   * for the negative windows (they abort before compounding), so a constant keeps the fixture focused on the date
   * coverage that drives the calendar-year checks.
   */
  protected static MonthlyReturns monthlyReturnsFor(String asOf, List<String> monthEnds) {
    List<DateBigDecimalValue> monthly = monthEnds.stream()
        .map(date -> new DateBigDecimalValue(date, new BigDecimal("1.0")))
        .toList();
    return monthlyReturns(monthly, DataProvider.MORNINGSTAR, asOf);
  }
}

package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.StandardDeviationResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.ErrorParams;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.performance.MonthlyReturns;
import com.fintex.wm.commons.domain.value.DateBigDecimalValue;
import com.fintex.wm.commons.error.ErrorResponse;
import com.fintex.wm.commons.error.Notification;
import com.fintex.wm.commons.error.Severity;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@Tag("e2e")
class StandardDeviationE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final SecurityIdentifier BMO_SUSTAINABLE_OPPORTUNITIES = new SecurityIdentifier("F00001F5DF",
      FiIdentifierType.MORNINGSTAR_ID);
  private static final String MISSING_NOVEMBER_2024 = "2024-11-30";
  private static final String MISSING_DECEMBER_2024 = "2024-12-31";
  private static final int MONTHS_IN_SERIES = 14;
  private static final BigDecimal TOLERANCE = new BigDecimal("0.0000000001");

  @Override
  protected String metricPath() {
    return CalculationMetric.STANDARD_DEVIATION.getValue();
  }

  @Override
  protected String requestBodyForSmsUnavailableScenario() {
    return writeJson(standardDeviationCommand());
  }

  @Override
  protected String requestBodyForPositiveSmsScenario() {
    return writeJson(standardDeviationCommand());
  }

  @Override
  protected String smsPositiveResponseBody() {
    return writeJson(List.of(holdingReturnsRow(monthlyReturns())));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    PeriodCommand command = standardDeviationCommand();
    command.setMetric(CalculationMetric.TRAILING_TOTAL_RETURNS);
    return writeJson(command);
  }

  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    StandardDeviationResult result = readJson(responseBody, StandardDeviationResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getPerformanceStartDate()).isEqualTo(LocalDate.parse("2024-01-31"));
    assertThat(result.getPerformanceEndDate()).isEqualTo(LocalDate.parse("2025-02-28"));
    assertThat(result.getStandardDeviation()).hasSize(1);
    assertThat(findPeriod(result).value()).isCloseTo(new BigDecimal("0.0124899960"), within(TOLERANCE));
  }

  @Test
  void shouldReturnBadRequestWithMissingMonthlyReturnError_whenHoldingReturnsContainGaps() {
    enqueueSmsMockResponse(writeJson(List.of(holdingReturnsRow(monthlyReturnsWithGaps()))));

    HttpResponse response = postCalculation(writeJson(standardDeviationCommand()));

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    ErrorResponse error = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(error.getNotifications()).hasSize(1);
    Notification notification = error.getNotifications().getFirst();
    assertThat(notification.getCode()).isEqualTo(ErrorCode.MISSING_MONTHLY_RETURN_FOR_DATE.getCode());
    assertThat(notification.getMessage()).isEqualTo(
        "The holding is missing monthly return values for date 2024-11-30, 2024-12-31");
    assertThat(notification.getDescription()).isEqualTo(
        "Monthly return is missing for the specified date");
    assertThat(notification.getAction()).isEqualTo(
        "Populate the monthly return for the missing date");
    assertThat(notification.getSeverity()).isEqualTo(Severity.ERROR);
    assertThat(notification.getMetadata())
        .containsOnlyKeys(ErrorParams.HOLDING_ID, "param-1")
        .containsEntry(ErrorParams.HOLDING_ID, "MUTUAL_FUND-F00001F5DF")
        .containsEntry("param-1", "2024-11-30, 2024-12-31");
  }

  private static TimeIntervalResult findPeriod(StandardDeviationResult result) {
    return result.getStandardDeviation().stream()
        .filter(entry -> "12".equals(entry.period()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing period 12"));
  }

  private static PeriodCommand standardDeviationCommand() {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(CalculationMetric.STANDARD_DEVIATION);
    command.setCurrency(Currency.CAD);
    command.setPeriods(Set.of("12"));
    command.setHoldings(List.of(new PortfolioHolding(new BigDecimal("100000"),
        FinancialInstrumentType.MUTUAL_FUND,
        Country.CANADA,
        BMO_SUSTAINABLE_OPPORTUNITIES)));
    return command;
  }

  private static SecurityAttributeResult<MonthlyReturns> holdingReturnsRow(List<DateBigDecimalValue> returns) {
    MonthlyReturns monthlyReturns = new MonthlyReturns();
    monthlyReturns.setReturns(returns);
    monthlyReturns.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return securityAttributeResult(BMO_SUSTAINABLE_OPPORTUNITIES, monthlyReturns);
  }

  private static List<DateBigDecimalValue> monthlyReturns() {
    return IntStream.range(0, MONTHS_IN_SERIES)
        .mapToObj(month -> LocalDate.of(2024, 1, 31).plusMonths(month))
        .map(date -> date.withDayOfMonth(date.lengthOfMonth()))
        .map(date -> dateValue(date, BigDecimal.valueOf(date.getMonthValue()).movePointLeft(1)))
        .toList();
  }

  private static List<DateBigDecimalValue> monthlyReturnsWithGaps() {
    return monthlyReturns().stream()
        .filter(value -> !MISSING_NOVEMBER_2024.equals(value.getDate()))
        .filter(value -> !MISSING_DECEMBER_2024.equals(value.getDate()))
        .collect(Collectors.toList());
  }

  private static DateBigDecimalValue dateValue(LocalDate date, BigDecimal value) {
    DateBigDecimalValue returnValue = new DateBigDecimalValue();
    returnValue.setDate(date.toString());
    returnValue.setValue(value);
    return returnValue;
  }
}

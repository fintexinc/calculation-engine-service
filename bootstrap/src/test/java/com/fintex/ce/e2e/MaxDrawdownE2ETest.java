package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.MaxDrawdownEntry;
import com.fintex.ce.model.domain.result.risk.MaxDrawdownResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.performance.MonthlyReturns;
import com.fintex.wm.commons.domain.value.DateBigDecimalValue;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static com.fintex.ce.e2e.PortfolioHoldingBuildHelper.holdingOfCountry;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.SIX_MTH;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.THREE_MTH;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.THREE_YR;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end HTTP-boundary coverage for the {@code max-drawdown} metric. Uses two CAD holdings of different instrument
 * and identifier types so the weighted-average blending path is exercised, while still needing exactly one Security
 * Master round trip (monthly returns) with no FX/treasury dependency.
 */
class MaxDrawdownE2ETest extends AbstractPortfolioCalculationE2ETest {

  // Two CAD holdings of different instrument/identifier types: no FX, but a real weighted-average blend of two series.
  private static final SecurityIdentifier XBAL = new SecurityIdentifier("XBAL", FiIdentifierType.TICKER);
  private static final SecurityIdentifier CCM4752 = new SecurityIdentifier("CCM4752", FiIdentifierType.FUNDSERV);

  /** Month-end bounds of the weighted-average portfolio returns produced by {@link #smsPositiveResponseBody()}. */
  private static final LocalDate RETURNS_FIRST_MONTH_END = LocalDate.of(2024, 1, 31);
  private static final LocalDate RETURNS_LAST_MONTH_END = LocalDate.of(2024, 12, 31);

  private static final String SINCE_CIPSD_PERIOD = TimePeriod.CIPSD.name();

  private static final String[] MONTH_ENDS_2024 = {
      "2024-01-31", "2024-02-29", "2024-03-31", "2024-04-30", "2024-05-31", "2024-06-30",
      "2024-07-31", "2024-08-31", "2024-09-30", "2024-10-31", "2024-11-30", "2024-12-31"};

  // Monthly returns (percent) per security. Both include declines so the blended growth curve has a real drawdown.
  private static final String[] XBAL_RETURNS_2024 = {
      "1.0", "0.3", "1.9", "-0.6", "2.4", "0.1", "1.3", "1.8", "-0.2", "2.1", "0.5", "1.6"};
  private static final String[] FUND_RETURNS_2024 = {
      "0.7", "0.9", "0.0", "-2.5", "1.3", "-0.8", "1.5", "-0.1", "-1.0", "1.4", "0.6", "1.2"};

  @Override
  protected String metricPath() {
    return CalculationMetric.MAX_DRAWDOWN.getValue();
  }

  @Override
  protected String requestBodyForSmsUnavailableScenario() {
    return writeJson(periodCommand(CalculationMetric.MAX_DRAWDOWN));
  }

  @Override
  protected String requestBodyForPositiveSmsScenario() {
    return writeJson(periodCommand(CalculationMetric.MAX_DRAWDOWN));
  }

  @Override
  protected String smsPositiveResponseBody() {
    return writeJson(List.of(
        holdingReturnsRow(XBAL, monthlyReturns(XBAL_RETURNS_2024)),
        holdingReturnsRow(CCM4752, monthlyReturns(FUND_RETURNS_2024))));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    return writeJson(periodCommand(CalculationMetric.SHARPE_RATIO));
  }

  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    MaxDrawdownResult result = readJson(responseBody, MaxDrawdownResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getPerformanceStartDate()).isEqualTo(RETURNS_FIRST_MONTH_END);
    assertThat(result.getPerformanceEndDate()).isEqualTo(RETURNS_LAST_MONTH_END);
    // No CIPSD was supplied, so exactly the requested one-year period is returned - never a since-CIPSD entry.
    assertThat(result.getMaxDrawdown())
        .extracting(MaxDrawdownEntry::period)
        .containsExactly(ONE_YR.name())
        .doesNotContain(SINCE_CIPSD_PERIOD);
    // Golden values captured from the deterministic pipeline for the seeded two-security blend. The blended growth
    // curve dips in April 2024 (both series decline that month), giving a single-month peak-to-trough drawdown that
    // recovers one month later.
    MaxDrawdownEntry twelveMonths = result.getMaxDrawdown().get(0);
    assertThat(twelveMonths.value()).isEqualByComparingTo("-0.0136");
    assertThat(twelveMonths.drawdownStartDate()).isEqualTo(LocalDate.of(2024, 4, 1));
    assertThat(twelveMonths.drawdownTroughDate()).isEqualTo(LocalDate.of(2024, 4, 30));
    assertThat(twelveMonths.recoveryTime()).isEqualTo(1);
  }

  @Test
  void shouldReturnBadRequest_whenCipsdIsBeforeAvailableReturnsRange() {
    enqueueSmsMockResponse(smsPositiveResponseBody());

    PeriodCommand command = periodCommand(CalculationMetric.MAX_DRAWDOWN);
    command.setCustomIntervalPsd(LocalDate.of(2023, 1, 31));

    var response = postCalculation(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(response.responseBody()).contains(ErrorCode.Codes.CIPSD_OUTSIDE_DATA_RANGE_ERROR);
    assertThat(response.responseBody())
        .contains("CIPSD 2023-01-31 is outside the available monthly returns range [2024-01-31, 2024-12-31]");
  }

  @Test
  void shouldReturnOk_whenCipsdIsWithinReturnsRange_thenIncludesSinceCipsdEntry() {
    enqueueSmsMockResponse(smsPositiveResponseBody());

    LocalDate cipsd = LocalDate.of(2024, 6, 30);
    PeriodCommand command = periodCommand(CalculationMetric.MAX_DRAWDOWN);
    command.setCustomIntervalPsd(cipsd);

    var response = postCalculation(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    MaxDrawdownResult result = readJson(response.responseBody(), MaxDrawdownResult.class);
    assertThat(result.getCustomIntervalPerformanceStartDate()).isEqualTo(cipsd);
    assertThat(result.getMaxDrawdown())
        .extracting(MaxDrawdownEntry::period)
        .contains(ONE_YR.name(), SINCE_CIPSD_PERIOD);
  }

  /**
   * A request with several time-interval periods, all within the available history, fans out to one drawdown entry per
   * requested period and raises no warnings, since every period fits the 12 months of returns.
   */
  @Test
  void shouldReturnEntryPerPeriod_whenMultiplePeriodsWithinRange() {
    enqueueSmsMockResponse(smsPositiveResponseBody());

    PeriodCommand command = periodCommand(CalculationMetric.MAX_DRAWDOWN);
    command.setPeriods(Set.of(THREE_MTH, SIX_MTH, ONE_YR));

    var response = postCalculation(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    MaxDrawdownResult result = readJson(response.responseBody(), MaxDrawdownResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getMaxDrawdown())
        .extracting(MaxDrawdownEntry::period)
        .containsExactlyInAnyOrder(THREE_MTH.name(), SIX_MTH.name(), ONE_YR.name());
  }

  /**
   * A requested period longer than the available monthly-returns history cannot be computed, so the metric still
   * responds 200 with a null value for that period plus an
   * {@link ErrorCode.Codes#INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD} (RET-008) warning rather than failing the request.
   */
  @Test
  void shouldReturnWarning_whenRequestedPeriodExceedsAvailableHistory() {
    enqueueSmsMockResponse(smsPositiveResponseBody());

    PeriodCommand command = periodCommand(CalculationMetric.MAX_DRAWDOWN);
    command.setPeriods(Set.of(THREE_YR)); // only 12 months of returns are available

    var response = postCalculation(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    MaxDrawdownResult result = readJson(response.responseBody(), MaxDrawdownResult.class);
    // The un-computable period yields a null-value entry accompanied by an insufficient-data warning.
    assertThat(result.getMaxDrawdown())
        .singleElement()
        .satisfies(entry -> {
          assertThat(entry.period()).isEqualTo(THREE_YR.name());
          assertThat(entry.value()).isNull();
        });
    assertThat(result.getWarnings())
        .singleElement()
        .satisfies(warning -> {
          assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD);
          // Message must carry both substituted values (requested period 36, only 12 available).
          assertThat(warning.getMessage())
              .contains(String.valueOf(THREE_YR.getMonths()))
              .contains("12");
        });
  }

  /**
   * An unsupported time-interval period is rejected up front with {@code 400 Bad Request} and
   * {@link ErrorCode.Codes#TIME_INTERVAL_PERIOD_NOT_SUPPORTED} (TIP-011), echoing the offending token and listing what
   * would have been accepted.
   *
   * <p>
   * Posted as raw JSON because a period is now a typed enum: the value cannot be put on the command object at all, and
   * the rejection happens while reading the body. That is the path a real caller takes, so it is the one worth
   * covering.
   */
  @Test
  void shouldReturnBadRequest_whenPeriodIsNotAKnownPeriod() {
    enqueueSmsMockResponse(smsPositiveResponseBody());

    String body = writeJson(periodCommand(CalculationMetric.MAX_DRAWDOWN))
        .replace("\"ONE_YR\"", "\"not-a-period\"");

    var response = postCalculation(body);

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(response.responseBody()).contains(ErrorCode.Codes.TIME_INTERVAL_PERIOD_NOT_SUPPORTED);
    assertThat(response.responseBody()).contains("not-a-period");
    // the message names the alternatives, so a caller can fix the request without reading our source
    assertThat(response.responseBody()).contains("TWENTY_YR");
  }

  private static PeriodCommand periodCommand(CalculationMetric metric) {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(metric);
    command.setCurrency(Currency.CAD);
    command.setPeriods(Set.of(ONE_YR));
    command.setCustomPed(LocalDate.parse("2024-12-31"));
    command.setHoldings(List.of(
        holdingOfCountry(XBAL, FinancialInstrumentType.ETF, Country.CANADA, "60000.00"),
        holdingOfCountry(CCM4752, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "40000.00")));
    return command;
  }

  private static SecurityAttributeResult<MonthlyReturns> holdingReturnsRow(SecurityIdentifier identifier,
      List<DateBigDecimalValue> returns) {
    MonthlyReturns response = new MonthlyReturns();
    response.setReturns(returns);
    response.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    SecurityAttributeResult<MonthlyReturns> result = new SecurityAttributeResult<>();
    result.setIdentifier(identifier);
    result.setData(response);
    return result;
  }

  private static List<DateBigDecimalValue> monthlyReturns(String[] percents) {
    return IntStream.range(0, MONTH_ENDS_2024.length)
        .mapToObj(i -> dateValue(MONTH_ENDS_2024[i], percents[i]))
        .toList();
  }

  private static DateBigDecimalValue dateValue(String date, String percent) {
    DateBigDecimalValue dv = new DateBigDecimalValue();
    dv.setDate(date);
    dv.setValue(new BigDecimal(percent));
    return dv;
  }
}

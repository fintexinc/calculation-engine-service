package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.result.fee.FeeComparison;
import com.fintex.ce.model.domain.result.fee.FeeSpendComparison;
import com.fintex.ce.model.domain.result.fee.MerComparisonResult;
import com.fintex.ce.model.dto.command.MerComparisonCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import static com.fintex.ce.e2e.PortfolioHoldingBuildHelper.etfCa;
import static com.fintex.ce.e2e.PortfolioHoldingBuildHelper.holdingOfCountry;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY_STRICT;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.WHOLE_PORTFOLIO;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.FIVE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.TEN_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.THREE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.TWENTY_YR;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boundary coverage for the fee comparison and its per-horizon projections. Amounts assume the shipped
 * {@code calculation.fee.projection} defaults — horizons 1 / 10 / 20 years and a 6% annual balance growth — so a change
 * to either default will surface here as a failing expectation rather than silently reshaping the response.
 *
 * <p>
 * Portfolio and benchmark attributes are fetched in two separate Security Master calls, in that order, so every
 * positive scenario enqueues two mock responses. The inherited failure scenarios enqueue only one: the portfolio fetch
 * fails first and the orchestrator then skips the benchmark call rather than re-hitting a Security Master that has just
 * failed.
 */
@Tag("e2e")
class MerBenchmarkComparisonE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final BigDecimal PORTFOLIO_VALUE = BigDecimal.valueOf(100_000);
  private static final String PORTFOLIO_TICKER = "XBAL";
  private static final String BENCHMARK_TICKER = "TNGBAL";

  // The mixed portfolio behind the inherited positive scenario: two funds at different rates and values, plus a
  // zero-fee stock large enough to halve the whole-portfolio rate. Fee dollars are 0.02 x 200k + 0.01 x 50k = 4,500.
  private static final String MIXED_ETF = "XBAL";
  private static final String MIXED_FUND = "CIG1101";
  private static final String MIXED_STOCK = "RY";
  private static final String BENCHMARK_GROWTH = "TNGGRW";

  @Override
  protected String metricPath() {
    return "mer-benchmark-comparison";
  }

  @Override
  protected String requestBodyForSmsUnavailableScenario() {
    return writeJson(comparisonCommand(FUNDS_ONLY, WHOLE_PORTFOLIO));
  }

  @Override
  protected String requestBodyForPositiveSmsScenario() {
    return writeJson(mixedPortfolioCommand());
  }

  @Override
  protected String smsPositiveResponseBody() {
    return feeResponse(new Fee(MIXED_ETF, "2.00"), new Fee(MIXED_FUND, "1.00"));
  }

  @Override
  protected void enqueueForPositiveSmsScenario() {
    enqueueSmsMockResponse(smsPositiveResponseBody());
    // a two-fund benchmark, so its rate is value-weighted rather than one fund's own MER:
    // 0.01 x 300k + 0.015 x 100k = 4,500 over 400k = 0.01125
    enqueueSmsMockResponse(feeResponse(new Fee(BENCHMARK_TICKER, "1.00"), new Fee(BENCHMARK_GROWTH, "1.50")));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    var command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setHoldings(List.of(etfCa(PORTFOLIO_TICKER, PORTFOLIO_VALUE.longValue())));
    command.setCurrency(Currency.CAD);
    return writeJson(command);
  }

  /**
   * The whole point of the mixed portfolio: the same holdings save money in one view and lose money in the other, and
   * both answers are right. Funds-only asks "what if the 250k already in funds moved to the benchmark" and saves;
   * whole-portfolio asks "what if all 500k did", which drags the zero-fee stock into a fee-bearing fund and costs more.
   * A single-fund portfolio cannot show this, because there both views share one asset base.
   */
  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    MerComparisonResult result = readJson(responseBody, MerComparisonResult.class);
    assertThat(result.getComparison()).containsOnlyKeys(FUNDS_ONLY, FUNDS_ONLY_STRICT, WHOLE_PORTFOLIO);

    // funds-only: 4,500 of fee dollars over the 250k held in funds = 1.8%, against the benchmark's weighted 1.125%
    FeeComparison fundsOnly = result.getComparison().get(FUNDS_ONLY);
    assertThat(fundsOnly.getFeeRate().portfolio()).isEqualByComparingTo("0.018");
    assertThat(fundsOnly.getFeeRate().benchmark()).isEqualByComparingTo("0.01125");
    assertThat(fundsOnly.getFeeRate().percentDifference()).isEqualByComparingTo("60");
    assertThat(fundsOnly.getFeeRate().equal()).isFalse();
    assertThat(fundsOnly.getSpend()).containsOnlyKeys(ONE_YR, TEN_YR, TWENTY_YR);
    assertHorizon(fundsOnly.getSpend().get(ONE_YR), "4500", "2812.5", "1687.5");
    assertHorizon(fundsOnly.getSpend().get(TEN_YR), "59313.5772407137", "37070.9857754461", "22242.5914652677");
    assertHorizon(fundsOnly.getSpend().get(TWENTY_YR), "165535.1604159630", "103459.4752599769", "62075.6851559861");

    // whole-portfolio: the same 4,500 spread over all 500k halves the rate to 0.9%, so the benchmark is now the dearer
    // side and every horizon reports a loss rather than a saving
    FeeComparison whole = result.getComparison().get(WHOLE_PORTFOLIO);
    assertThat(whole.getFeeRate().portfolio()).isEqualByComparingTo("0.009");
    assertThat(whole.getFeeRate().benchmark()).isEqualByComparingTo("0.01125");
    assertThat(whole.getFeeRate().percentDifference()).isEqualByComparingTo("-20");
    assertHorizon(whole.getSpend().get(ONE_YR), "4500", "5625", "-1125");
    assertHorizon(whole.getSpend().get(TEN_YR), "59313.5772407137", "74141.9715508922", "-14828.3943101784");
    assertHorizon(whole.getSpend().get(TWENTY_YR), "165535.1604159630", "206918.9505199537", "-41383.7901039907");
    assertThat(whole.getSpend().values()).allSatisfy(horizon -> assertThat(horizon.savings()).isNegative());

    // the portfolio's fee dollars do not depend on the view, only the base they are spread over does
    assertThat(whole.getSpend().get(TWENTY_YR).portfolio())
        .isEqualByComparingTo(fundsOnly.getSpend().get(TWENTY_YR).portfolio());

    // strict mode: every fund resolved through its primary MER, so nothing is withheld and it mirrors funds-only
    FeeComparison strict = result.getComparison().get(FUNDS_ONLY_STRICT);
    assertThat(strict.getFeeRate().portfolio()).isEqualByComparingTo("0.018");
    assertHorizon(strict.getSpend().get(TWENTY_YR), "165535.1604159630", "103459.4752599769", "62075.6851559861");

    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldReportNegativeSavingsAtEveryHorizon_whenTheBenchmarkFundIsDearerThanThePortfolio() {
    enqueueSmsMockResponse(feeResponse(PORTFOLIO_TICKER, "1.00"));
    enqueueSmsMockResponse(feeResponse(BENCHMARK_TICKER, "2.50"));

    var response = postCalculation(writeJson(comparisonCommand(FUNDS_ONLY)));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    FeeComparison fundsOnly = readJson(response.responseBody(), MerComparisonResult.class)
        .getComparison().get(FUNDS_ONLY);
    assertThat(fundsOnly.getFeeRate().percentDifference()).isEqualByComparingTo("-60");
    assertHorizon(fundsOnly.getSpend().get(ONE_YR), "1000", "2500", "-1500");
    assertHorizon(fundsOnly.getSpend().get(TEN_YR), "13180.7949423808", "32951.9873559521", "-19771.1924135712");
    assertHorizon(fundsOnly.getSpend().get(TWENTY_YR), "36785.5912035473", "91963.9780088683", "-55178.3868053210");
    assertThat(fundsOnly.getSpend().values()).allSatisfy(horizon -> assertThat(horizon.savings()).isNegative());
  }

  private static void assertHorizon(FeeSpendComparison actual, String portfolioSpend, String benchmarkSpend,
      String savings) {
    assertThat(actual.portfolio()).isEqualByComparingTo(portfolioSpend);
    assertThat(actual.benchmark()).isEqualByComparingTo(benchmarkSpend);
    assertThat(actual.savings()).isEqualByComparingTo(savings);
  }

  @Test
  void shouldProjectTheRequestedHorizons_whenTheRequestSuppliesThemInsteadOfTheConfiguredDefaults() {
    enqueueSmsMockResponse(feeResponse(PORTFOLIO_TICKER, "2.00"));
    enqueueSmsMockResponse(feeResponse(BENCHMARK_TICKER, "1.00"));
    MerComparisonCommand command = comparisonCommand(FUNDS_ONLY);
    command.setProjectionPeriods(new LinkedHashSet<>(List.of(THREE_YR, FIVE_YR)));

    var response = postCalculation(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    FeeComparison fundsOnly = readJson(response.responseBody(), MerComparisonResult.class)
        .getComparison().get(FUNDS_ONLY);
    // 2000 vs 1000 a year on a balance growing at the shipped 6%: 3 and 5 years, not the configured 1/10/20.
    // Factors are exact here: ((1.06^3)-1)/0.06 = 3.1836 and ((1.06^5)-1)/0.06 = 5.63709296
    assertThat(fundsOnly.getSpend()).containsOnlyKeys(THREE_YR, FIVE_YR);
    assertHorizon(fundsOnly.getSpend().get(THREE_YR), "6367.2", "3183.6", "3183.6");
    assertHorizon(fundsOnly.getSpend().get(FIVE_YR), "11274.18592", "5637.09296", "5637.09296");
  }

  /**
   * A period below one year is no longer expressible, so what is worth covering is the case that is: a real
   * {@code TimePeriod} the fee metrics do not project over. {@code SEVEN_YR} has a length and the arithmetic would
   * handle it — it is simply not on the reporting ladder.
   */
  @Test
  void shouldReturnBadRequest_whenARequestedPeriodIsOffTheFeeLadder() {
    MerComparisonCommand command = comparisonCommand(FUNDS_ONLY);
    command.setProjectionPeriods(new LinkedHashSet<>(List.of(TEN_YR, TimePeriod.SEVEN_YR)));

    var response = postCalculation(writeJson(command));

    // rejected at the boundary, so Security Master is never called and the caller gets the field-level reason
    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(response.responseBody())
        .contains(ErrorCode.TIME_INTERVAL_PERIOD_NOT_SUPPORTED.getCode())
        .contains(TimePeriod.SEVEN_YR.name())
        // the message lists the ladder, so the caller can pick a period without reading our source
        .contains(TEN_YR.name());
  }

  /** A length-less period cannot be projected forward at all, and is refused for the same reason. */
  @Test
  void shouldReturnBadRequest_whenARequestedPeriodHasNoLength() {
    MerComparisonCommand command = comparisonCommand(FUNDS_ONLY);
    command.setProjectionPeriods(new LinkedHashSet<>(List.of(TEN_YR, TimePeriod.YTD)));

    var response = postCalculation(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(response.responseBody())
        .contains(ErrorCode.TIME_INTERVAL_PERIOD_NOT_SUPPORTED.getCode())
        .contains(TimePeriod.YTD.name());
  }

  private static MerComparisonCommand comparisonCommand(FeeAggregationMode... modes) {
    var command = new MerComparisonCommand();
    command.setMetric(CalculationMetric.MER_BENCHMARK_COMPARISON);
    command.setParameterTypes(List.of(modes));
    command.setHoldings(List.of(etfCa(PORTFOLIO_TICKER, PORTFOLIO_VALUE.longValue())));
    command.setBenchmarkHoldings(List.of(etfCa(BENCHMARK_TICKER, PORTFOLIO_VALUE.longValue())));
    command.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return command;
  }

  /**
   * A portfolio whose two views cannot collapse into one another: 250k across two funds charging different rates, and
   * an equally large zero-fee stock that only widens the whole-portfolio denominator. The benchmark is two funds, so
   * its rate has to be value-weighted rather than read off a single holding.
   */
  private static MerComparisonCommand mixedPortfolioCommand() {
    var command = new MerComparisonCommand();
    command.setMetric(CalculationMetric.MER_BENCHMARK_COMPARISON);
    command.setParameterTypes(List.of(FUNDS_ONLY, FUNDS_ONLY_STRICT, WHOLE_PORTFOLIO));
    command.setHoldings(List.of(
        holdingOfCountry(new SecurityIdentifier(MIXED_ETF, FiIdentifierType.TICKER), FinancialInstrumentType.ETF,
            Country.CANADA, BigDecimal.valueOf(200_000)),
        holdingOfCountry(new SecurityIdentifier(MIXED_FUND, FiIdentifierType.TICKER),
            FinancialInstrumentType.MUTUAL_FUND,
            Country.CANADA, BigDecimal.valueOf(50_000)),
        holdingOfCountry(new SecurityIdentifier(MIXED_STOCK, FiIdentifierType.TICKER), FinancialInstrumentType.STOCK,
            Country.CANADA, BigDecimal.valueOf(250_000))));
    command.setBenchmarkHoldings(List.of(
        holdingOfCountry(new SecurityIdentifier(BENCHMARK_TICKER, FiIdentifierType.TICKER),
            FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.valueOf(300_000)),
        holdingOfCountry(new SecurityIdentifier(BENCHMARK_GROWTH, FiIdentifierType.TICKER),
            FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.valueOf(100_000))));
    command.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return command;
  }

  /**
   * Security Master reports fee fields in percentage form (2.00 meaning 2%); the adapter converts them to ratios. The
   * currency datapoint is mandatory for a MER-bearing holding — without it the FX step hard-fails with CUR-003.
   */
  private static String feeResponse(String ticker, String merPercent) {
    return feeResponse(new Fee(ticker, merPercent));
  }

  private static String feeResponse(Fee... fees) {
    List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);
    return writeJson(Arrays.stream(fees)
        .map(fee -> new SmsSecurityDataResponse(
            new SecurityIdentifier(fee.ticker(), FiIdentifierType.TICKER),
            new SmsFeeData(
                new SmsDatapoint(new BigDecimal(fee.merPercent()), providers),
                new SmsCurrencyDatapoint(Currency.CAD, providers))))
        .toList());
  }

  /**
   * One fund's MER as Security Master states it, in percentage form. The zero-fee stock is deliberately absent from
   * every response: only MER-bearing holdings require fee data, and a holding contributing no fee needs no currency
   * either, so its market value reaches the whole-portfolio base unconverted — which in this single-currency test is
   * exactly its stated value.
   */
  private record Fee(String ticker, String merPercent) {
  }

  private record SmsSecurityDataResponse(SecurityIdentifier identifier, SmsFeeData data) {
  }

  private record SmsFeeData(SmsDatapoint managementExpenseRatio, SmsCurrencyDatapoint currency) {
  }

  private record SmsDatapoint(BigDecimal value, List<DataProvider> dataProviders) {
  }

  // wm-commons CurrencyDatapoint stores the value in a field literally named "type", so the JSON must say
  // {"type": "CAD"} for the engine to receive it.
  private record SmsCurrencyDatapoint(Currency type, List<DataProvider> dataProviders) {
  }
}

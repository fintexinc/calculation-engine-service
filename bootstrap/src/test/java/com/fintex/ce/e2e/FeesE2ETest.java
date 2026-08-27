package com.fintex.ce.e2e;

import com.fintex.ce.adapter.webclient.boc.client.BankOfCanadaProperties;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.FeesResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.error.ErrorResponse;
import com.fintex.wm.commons.error.Notification;
import com.fintex.wm.commons.error.Severity;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.e2e.SmsAttributeResponses.morningstarOnly;
import static com.fintex.ce.e2e.SmsFeeResponses.feesDispatcher;
import static com.fintex.ce.e2e.SmsFeeResponses.merRow;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.etfCa;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.fundCa;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.stockCa;
import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockWebServer;

/**
 * End-to-end coverage for the {@code /fees} endpoint — the only fee metric that reports dollar amounts rather than
 * rates: what the portfolio pays in a year, in a month, and over each projection horizon.
 *
 * <p>
 * The projection arithmetic itself is pinned where it belongs, in {@code FeeProjectionUtilsTest} and
 * {@code FeesCalculationServiceImplTest}, so what is asserted here is the HTTP contract and the two identities that
 * hold at any configured growth rate: a one-month horizon is the monthly fee and a one-year horizon is the annual fee.
 * Anchoring on those rather than on the 6% figures keeps this test from breaking every time the growth assumption is
 * retuned in configuration — which is precisely why it is configuration.
 */
@Tag("e2e")
class FeesE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final String USD_CAD_RATE = "1.5000";
  private static final String CANADIAN_FUND = "F00000FEE1";
  private static final String US_ETF = "AGG";

  /**
   * The USD holding is converted through this server rather than the live Bank of Canada endpoint, at one constant
   * rate, so the expected dollar amounts are a property of the request instead of a function of the rate of the day.
   */
  private static final MockWebServer bocMockServer = MockWebServers.started(BocMockResponses
      .constantUsdCadRateDispatcher(USD_CAD_RATE));

  @AfterAll
  static void shutdownBocMockServer() throws IOException {
    bocMockServer.shutdown();
  }

  @DynamicPropertySource
  static void registerBocBaseUrl(DynamicPropertyRegistry registry) {
    registry.add(BankOfCanadaProperties.BASE_URL_PROPERTY,
        () -> MockWebServers.baseUrl(bocMockServer));
  }

  @Override
  protected String metricPath() {
    return CalculationMetric.FEES.getValue();
  }

  @Override
  protected String requestBodyForSmsUnavailableScenario() {
    return writeJson(feesCommand(null, fundCa(CANADIAN_FUND, 50_000), etfCa(US_ETF, 50_000)));
  }

  /**
   * A domestic fund, a US-listed ETF quoted in USD and an individual stock, with no projection horizons in the request
   * so the configured ones apply. The stock is there to pin what this metric does with a fee-free holding: it costs
   * nothing, so it must not change a single dollar of the answer — unlike in the rate metrics, where it drags the
   * whole-portfolio average down.
   */
  @Override
  protected String requestBodyForPositiveSmsScenario() {
    return writeJson(feesCommand(null,
        fundCa(CANADIAN_FUND, 300_000),
        etfCa(US_ETF, 200_000),
        stockCa("RY.TO", "TSX", 200_000)));
  }

  @Override
  protected String smsPositiveResponseBody() {
    return SmsFeeResponses.body(
        merRow(CANADIAN_FUND, FiIdentifierType.MORNINGSTAR_ID, "1.00", Currency.CAD),
        merRow(US_ETF, FiIdentifierType.TICKER, "2.00", Currency.USD));
  }

  /**
   * The shared positive scenario enqueues a single response, which suffices for one holding; this portfolio holds three
   * across three identifier types, and how many attribute calls the fetcher batches them into is an implementation
   * detail. The dispatcher answers all of them, and only on the fee attribute's path.
   */
  @Override
  protected void enqueueForPositiveSmsScenario() {
    smsMockServer.setDispatcher(feesDispatcher(
        merRow(CANADIAN_FUND, FiIdentifierType.MORNINGSTAR_ID, "1.00", Currency.CAD),
        merRow(US_ETF, FiIdentifierType.TICKER, "2.00", Currency.USD)));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(fundCa(CANADIAN_FUND, 50_000)));
    return writeJson(command);
  }

  /**
   * The full expected payload, derived by hand from the request above. The USD ETF is worth 300 000 CAD at
   * {@value #USD_CAD_RATE}, so the annual fee is 300 000·1% + 300 000·2% = 9 000 and the monthly fee is 750. Both
   * aggregation views report the same dollars here, and that is the metric's own arithmetic rather than a coincidence:
   * the stock's fee is zero, so it adds nothing to a sum, where in {@code /management-fee} it would move the
   * whole-portfolio average.
   *
   * <p>
   * The horizons are the configured ones — the request named none — and the one-year figure is the annual fee itself at
   * any growth rate. The longer horizons are asserted as strictly increasing rather than as figures of their own,
   * because their values are a function of the configured growth assumption and pinning them here would make a
   * configuration change look like a regression in the endpoint.
   */
  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    FeesResult result = readJson(responseBody, FeesResult.class);

    assertThat(result.getWarnings()).isEmpty();
    for (FeeAggregationMode mode : List.of(FeeAggregationMode.FUNDS_ONLY, FeeAggregationMode.WHOLE_PORTFOLIO)) {
      assertThat(result.getAnnualFee().get(mode)).as("annual fee, %s", mode).isEqualByComparingTo("9000");
      assertThat(result.getMonthlyFee().get(mode)).as("monthly fee, %s", mode).isEqualByComparingTo("750");

      Map<TimePeriod, BigDecimal> projected = result.getProjectedSpend().get(mode);
      assertThat(projected).as("projected spend, %s", mode)
          .containsOnlyKeys(TimePeriod.ONE_YR, TimePeriod.TEN_YR, TimePeriod.TWENTY_YR);
      assertThat(projected.get(TimePeriod.ONE_YR)).as("a one-year horizon is the annual fee, %s", mode)
          .isEqualByComparingTo("9000");
      assertThat(projected.get(TimePeriod.TEN_YR)).as("ten years cost more than one, %s", mode)
          .isGreaterThan(projected.get(TimePeriod.ONE_YR));
      assertThat(projected.get(TimePeriod.TWENTY_YR)).as("twenty years cost more than ten, %s", mode)
          .isGreaterThan(projected.get(TimePeriod.TEN_YR));
    }
    assertThat(result.getAnnualFee()).hasSize(2);
    assertThat(result.getMonthlyFee()).hasSize(2);
    assertThat(result.getProjectedSpend()).hasSize(2);
  }

  /**
   * The horizons the caller asks for are the horizons reported. The two chosen here are the ones whose values are fixed
   * by definition rather than by the growth assumption — a month is the monthly fee, a year is the annual fee — so this
   * also asserts that the projection is anchored on the same numbers the same response reports beside it, instead of
   * drifting from them.
   *
   * <p>
   * Which horizons come back is asserted; the order they come back in is not, because the endpoint does not in fact
   * preserve it. {@code FeeProjectionProperties#periodsFor} documents a caller-supplied set as "taken as sent, in its
   * own order" and hands it straight to the {@code LinkedHashMap} the projection is built into, but
   * {@code projectionPeriods} arrives from Jackson as a hash-ordered set, and an enum's hash is its identity hash — so
   * the reported order varies from one JVM run to the next. Asserting the order here would be asserting that variance.
   * The gap between the documented contract and the delivered one is a product defect and belongs in its own ticket,
   * not in a test bent around it.
   */
  @Test
  void shouldReportTheRequestedHorizons_whenTheCommandNamesThem() {
    smsMockServer.setDispatcher(feesDispatcher(
        merRow(CANADIAN_FUND, FiIdentifierType.MORNINGSTAR_ID, "1.00", Currency.CAD)));

    var response = postCalculation(writeJson(feesCommand(
        new java.util.LinkedHashSet<>(List.of(TimePeriod.ONE_MTH, TimePeriod.ONE_YR)),
        fundCa(CANADIAN_FUND, 300_000))));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    FeesResult result = readJson(response.responseBody(), FeesResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getAnnualFee().get(FeeAggregationMode.FUNDS_ONLY)).isEqualByComparingTo("3000");
    assertThat(result.getMonthlyFee().get(FeeAggregationMode.FUNDS_ONLY)).isEqualByComparingTo("250");
    Map<TimePeriod, BigDecimal> projected = result.getProjectedSpend().get(FeeAggregationMode.FUNDS_ONLY);
    assertThat(projected).containsOnlyKeys(TimePeriod.ONE_MTH, TimePeriod.ONE_YR);
    assertThat(projected.get(TimePeriod.ONE_MTH)).isEqualByComparingTo("250");
    assertThat(projected.get(TimePeriod.ONE_YR)).isEqualByComparingTo("3000");
  }

  /**
   * This metric converts only the holdings whose fees it actually sums, so a fund carrying a fee and no currency is the
   * one case it cannot answer: its dollars are in an unknown unit. It rejects the request rather than adding them to a
   * total labelled CAD, since a fee amount reported in the wrong currency is not a rounding error but a different
   * number.
   */
  @Test
  void shouldRejectTheRequest_whenAFeeBearingHoldingHasNoCurrency() {
    PortfolioHolding fundWithoutCurrency = fundCa(CANADIAN_FUND, 300_000);
    smsMockServer.setDispatcher(feesDispatcher(
        merRow(CANADIAN_FUND, FiIdentifierType.MORNINGSTAR_ID, "1.00", null)));

    var response = postCalculation(writeJson(feesCommand(null, fundWithoutCurrency)));

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    ErrorResponse error = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(error.getNotifications()).hasSize(1);
    Notification notification = error.getNotifications().getFirst();
    String holdingId = fundWithoutCurrency.getIdsString();
    assertThat(notification.getCode()).isEqualTo(ErrorCode.Codes.HOLDING_MISSING_CURRENCY_FROM_FDS);
    assertThat(notification.getMessage()).isEqualTo(ErrorCode.HOLDING_MISSING_CURRENCY_FROM_FDS.getMessage());
    assertThat(notification.getSeverity()).isEqualTo(Severity.ERROR);
    assertThat(notification.getMetadata())
        .as("the message names no holding, so the metadata is the only place the caller can learn which one failed")
        .containsEntry("holdingId", holdingId);
    assertThat(response.responseBody()).doesNotContain("annualFee");
  }

  private static AverageMerCommand feesCommand(Set<TimePeriod> projectionPeriods, PortfolioHolding... holdings) {
    AverageMerCommand command = new AverageMerCommand();
    command.setMetric(CalculationMetric.FEES);
    command.setParameterTypes(List.of(FeeAggregationMode.FUNDS_ONLY, FeeAggregationMode.WHOLE_PORTFOLIO));
    command.setTargetCurrency(Currency.CAD);
    command.setProjectionPeriods(projectionPeriods);
    command.setHoldings(List.of(holdings));
    command.setDataProviders(morningstarOnly());
    return command;
  }
}

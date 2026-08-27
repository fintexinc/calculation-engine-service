package com.fintex.ce.e2e;

import com.fintex.ce.adapter.webclient.boc.client.BankOfCanadaProperties;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.ManagementFeeResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.currency.Currency;
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
import java.util.List;

import static com.fintex.ce.e2e.SmsAttributeResponses.morningstarOnly;
import static com.fintex.ce.e2e.SmsFeeResponses.currencyOnlyRow;
import static com.fintex.ce.e2e.SmsFeeResponses.feesDispatcher;
import static com.fintex.ce.e2e.SmsFeeResponses.managementFeeRow;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.etfCa;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.fundCa;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.stockCa;
import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockWebServer;

/**
 * End-to-end coverage for the {@code /management-fee} endpoint. It reports the portfolio's weighted average management
 * fee — the fee the manager charges, which is a different Security Master datapoint from the MER the {@code /mer}
 * endpoint reads, and one with no fallback: a fund whose management fee is absent is an error rather than a warning.
 *
 * <p>
 * The two aggregation modes are what make the metric worth an endpoint of its own, and the positive scenario is built
 * so they cannot come back equal: a stock carries no management fee, so it drags the whole-portfolio average below the
 * funds-only one. It also holds a USD fund, so the weights depend on the currency arriving from the fee row — the one
 * field of this attribute whose wire name differs from the domain class, and therefore the one a unit test cannot
 * catch.
 */
@Tag("e2e")
class ManagementFeeE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final String USD_CAD_RATE = "1.5000";
  private static final String CANADIAN_FUND = "F00000MGT1";
  private static final String US_ETF = "AGG";

  /**
   * The USD holding is converted through this server rather than the live Bank of Canada endpoint, at one constant
   * rate, so the expected averages are a property of the request instead of a function of the rate of the day.
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
    return CalculationMetric.MANAGEMENT_FEE.getValue();
  }

  @Override
  protected String requestBodyForSmsUnavailableScenario() {
    return writeJson(managementFeeCommand(fundCa(CANADIAN_FUND, 50_000), etfCa(US_ETF, 50_000)));
  }

  /**
   * A domestic fund, a US-listed ETF quoted in USD and an individual stock. The stock is the holding that separates the
   * two aggregation modes: it belongs in the whole-portfolio denominator at 0% and outside the funds-only one.
   */
  @Override
  protected String requestBodyForPositiveSmsScenario() {
    return writeJson(managementFeeCommand(
        fundCa(CANADIAN_FUND, 300_000),
        etfCa(US_ETF, 200_000),
        stockCa("RY.TO", "TSX", 200_000)));
  }

  @Override
  protected String smsPositiveResponseBody() {
    return SmsFeeResponses.body(
        managementFeeRow(CANADIAN_FUND, FiIdentifierType.MORNINGSTAR_ID, "1.00", Currency.CAD),
        managementFeeRow(US_ETF, FiIdentifierType.TICKER, "2.00", Currency.USD));
  }

  /**
   * The shared positive scenario enqueues a single response, which suffices for one holding; this portfolio holds three
   * across three identifier types, and how many attribute calls the fetcher batches them into is an implementation
   * detail. The dispatcher answers all of them, and only on the fee attribute's path.
   */
  @Override
  protected void enqueueForPositiveSmsScenario() {
    smsMockServer.setDispatcher(feesDispatcher(
        managementFeeRow(CANADIAN_FUND, FiIdentifierType.MORNINGSTAR_ID, "1.00", Currency.CAD),
        managementFeeRow(US_ETF, FiIdentifierType.TICKER, "2.00", Currency.USD)));
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
   * {@value #USD_CAD_RATE}, so the fee dollars are 300 000·1% + 300 000·2% = 9 000. Funds-only divides that by the 600
   * 000 the two funds are worth: 1.5%. Whole-portfolio divides it by all 800 000, the stock included at 0%: 1.125%.
   *
   * <p>
   * Both values are wrong in a specific way if the currency does not arrive: at 200 000 unconverted the ETF weighs less
   * and funds-only comes back at 1.4% instead of 1.5%.
   */
  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    ManagementFeeResult result = readJson(responseBody, ManagementFeeResult.class);

    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getManagementFee())
        .hasSize(2)
        .hasEntrySatisfying(FeeAggregationMode.FUNDS_ONLY,
            fee -> assertThat(fee).isEqualByComparingTo("0.015"))
        .hasEntrySatisfying(FeeAggregationMode.WHOLE_PORTFOLIO,
            fee -> assertThat(fee).isEqualByComparingTo("0.01125"));
  }

  /**
   * A fund whose management fee Security Master does not carry is rejected outright rather than reported as 0% or
   * warned about. That is the one behaviour separating this metric from {@code /mer}, which falls back to another
   * datapoint and warns; here there is nothing to fall back to, and quietly averaging a missing fee in as zero would
   * understate every fee the client is shown.
   */
  @Test
  void shouldRejectTheRequest_whenAFundCarriesNoManagementFee() {
    PortfolioHolding fundWithoutFee = fundCa(CANADIAN_FUND, 100_000);
    smsMockServer.setDispatcher(feesDispatcher(
        currencyOnlyRow(CANADIAN_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.CAD)));

    var response = postCalculation(writeJson(managementFeeCommand(fundWithoutFee)));

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    ErrorResponse error = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(error.getNotifications()).hasSize(1);
    Notification notification = error.getNotifications().getFirst();
    String holdingId = fundWithoutFee.getIdsString();
    assertThat(notification.getCode()).isEqualTo(ErrorCode.Codes.MISSING_MANAGEMENT_FEE);
    assertThat(notification.getMessage()).isEqualTo(ErrorCode.MISSING_MANAGEMENT_FEE.getMessage());
    assertThat(notification.getDescription()).isEqualTo(ErrorCode.MISSING_MANAGEMENT_FEE.getDescription());
    assertThat(notification.getAction()).isEqualTo(ErrorCode.MISSING_MANAGEMENT_FEE.getAction());
    assertThat(notification.getSeverity()).isEqualTo(Severity.ERROR);
    assertThat(notification.getMetadata())
        .as("the message names no holding, so the metadata is the only place the caller can learn which one failed")
        .containsEntry("holdingId", holdingId);
    assertThat(response.responseBody()).doesNotContain("managementFee\":{");
  }

  /**
   * A portfolio holding no fund has no funds-only answer, and the metric says so with a null rather than with a zero —
   * a zero would read as a portfolio whose managers charge nothing. The whole-portfolio view does have an answer for
   * the same request, and it is zero, because every holding in it genuinely carries no management fee.
   */
  @Test
  void shouldReportNullFundsOnly_whenThePortfolioHoldsNoFund() {
    smsMockServer.setDispatcher(feesDispatcher());

    var response = postCalculation(writeJson(managementFeeCommand(stockCa("RY.TO", "TSX", 200_000))));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    ManagementFeeResult result = readJson(response.responseBody(), ManagementFeeResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getManagementFee())
        .hasSize(2)
        .containsEntry(FeeAggregationMode.FUNDS_ONLY, null)
        .hasEntrySatisfying(FeeAggregationMode.WHOLE_PORTFOLIO,
            fee -> assertThat(fee).isEqualByComparingTo("0"));
  }

  private static AverageMerCommand managementFeeCommand(PortfolioHolding... holdings) {
    AverageMerCommand command = new AverageMerCommand();
    command.setMetric(CalculationMetric.MANAGEMENT_FEE);
    command.setParameterTypes(List.of(FeeAggregationMode.FUNDS_ONLY, FeeAggregationMode.WHOLE_PORTFOLIO));
    command.setTargetCurrency(Currency.CAD);
    command.setHoldings(List.of(holdings));
    command.setDataProviders(morningstarOnly());
    return command;
  }
}

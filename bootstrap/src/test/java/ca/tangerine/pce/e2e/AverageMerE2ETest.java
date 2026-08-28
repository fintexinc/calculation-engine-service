package ca.tangerine.pce.e2e;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.enumeration.FeeAggregationMode;
import ca.tangerine.pce.model.domain.result.fee.AverageMerResult;
import ca.tangerine.pce.model.dto.command.AverageMerCommand;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.error.ErrorResponse;
import ca.tangerine.wm.commons.error.Notification;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static ca.tangerine.pce.e2e.E2EPortfolios.etf;
import static ca.tangerine.pce.e2e.MicFeeResponses.currencyOnlyRow;
import static ca.tangerine.pce.e2e.MicFeeResponses.managementFeeRow;
import static ca.tangerine.pce.e2e.MicFeeResponses.merRow;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
class AverageMerE2ETest extends AbstractPortfolioCalculationE2ETest {

  @Override
  protected String metricPath() {
    return "mer";
  }

  @Override
  protected String requestBodyForMicUnavailableScenario() {
    var command = new AverageMerCommand();
    command.setMetric(CalculationMetric.MER);
    command.setParameterTypes(List.of(FeeAggregationMode.FUNDS_ONLY, FeeAggregationMode.WHOLE_PORTFOLIO));
    command.setHoldings(List.of(
        etf("XBAL", 50_000),
        etf("VCNS", 75_000)));
    command.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return writeJson(command);
  }

  @Override
  protected String requestBodyForPositiveMicScenario() {
    var command = new AverageMerCommand();
    command.setMetric(CalculationMetric.MER);
    command.setParameterTypes(List.of(FeeAggregationMode.FUNDS_ONLY, FeeAggregationMode.WHOLE_PORTFOLIO));
    command.setHoldings(List.of(
        etf("ETF-A", 300_000),
        etf("ETF-B", 100_000)));
    command.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return writeJson(command);
  }

  @Override
  protected String micPositiveResponseBody() {
    // MIC returns fee fields in percentage form (e.g. 2.25 meaning 2.25%). FeesMapper converts to ratio form
    // (0.0225) for the rest of the engine — the expected output below is in ratio form. The currency datapoint is
    // required for MER-bearing holdings — without it, the FX-conversion step hard-fails with CUR-003.
    return MicFeeResponses.body(
        merRow("ETF-A", FiIdentifierType.TICKER, "1.00", Currency.CAD),
        merRow("ETF-B", FiIdentifierType.TICKER, "2.00", Currency.CAD));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    var command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setHoldings(List.of(etf("XBAL", 50_000)));
    command.setCurrency(Currency.CAD);
    return writeJson(command);
  }

  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    AverageMerResult result = readJson(responseBody, AverageMerResult.class);
    assertThat(result.getManagementExpenseRatio())
        .hasSize(2)
        .containsEntry(
            FeeAggregationMode.FUNDS_ONLY,
            new BigDecimal("0.0125000000"))
        .containsEntry(
            FeeAggregationMode.WHOLE_PORTFOLIO,
            new BigDecimal("0.0125000000"));
    assertThat(result.getWarnings())
        .extracting(Notification::getCode)
        .containsExactly(ErrorCode.Codes.PORTFOLIO_MISSING_CURRENCY);
  }

  @Test
  void shouldRejectUsEtf_whenBothNetAndGrossExpenseRatiosAreMissing() {
    List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);
    String holdingId = "US-ETF-NO-EXPENSE-RATIOS";
    enqueueMicMockResponse(MicFeeResponses.body(
        currencyOnlyRow(holdingId, FiIdentifierType.TICKER, Currency.USD)));

    var command = new AverageMerCommand();
    command.setMetric(CalculationMetric.MER);
    command.setParameterTypes(
        List.of(
            FeeAggregationMode.FUNDS_ONLY,
            FeeAggregationMode.WHOLE_PORTFOLIO));
    command.setHoldings(
        List.of(etf(holdingId, Country.USA, 100_000)));
    command.setDataProviders(providers);

    var response = postCalculation(writeJson(command));
    assertThat(response.status().value())
        .isEqualTo(HttpStatus.BAD_REQUEST.value());
    ErrorResponse error = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(error.getNotifications()).hasSize(1);
    assertThat(error.getNotifications().getFirst().getCode())
        .isEqualTo(ErrorCode.Codes.MISSING_NER_AND_GER);
    assertThat(response.responseBody())
        .doesNotContain("managementExpenseRatio");
  }

  @Test
  void shouldFallbackToManagementFee_whenManagementExpenseRatioIsMissing() {
    List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);
    BigDecimal managementFeePercentage = new BigDecimal("1.00");
    BigDecimal expectedManagementFeeRatio = managementFeePercentage.movePointLeft(2).setScale(10);
    enqueueMicMockResponse(MicFeeResponses.body(managementFeeRow("ETF-MISSING-MER", FiIdentifierType.TICKER,
        managementFeePercentage.toPlainString(), Currency.CAD)));
    var command = new AverageMerCommand();
    command.setMetric(CalculationMetric.MER);
    command.setParameterTypes(
        List.of(
            FeeAggregationMode.FUNDS_ONLY,
            FeeAggregationMode.WHOLE_PORTFOLIO));
    command.setHoldings(
        List.of(etf("ETF-MISSING-MER", 100_000)));
    command.setDataProviders(providers);

    var response = postCalculation(writeJson(command));
    assertThat(response.status().value())
        .isEqualTo(HttpStatus.OK.value());
    AverageMerResult result = readJson(response.responseBody(), AverageMerResult.class);
    assertThat(result.getManagementExpenseRatio())
        .hasSize(2)
        .containsEntry(
            FeeAggregationMode.FUNDS_ONLY,
            expectedManagementFeeRatio)
        .containsEntry(
            FeeAggregationMode.WHOLE_PORTFOLIO,
            expectedManagementFeeRatio);
    assertThat(result.getWarnings())
        .extracting(notification -> notification.getCode())
        .containsExactly(
            ErrorCode.Codes.MISSING_MANAGEMENT_EXPENSE_RATIO,
            ErrorCode.Codes.PORTFOLIO_MISSING_CURRENCY);
  }
}

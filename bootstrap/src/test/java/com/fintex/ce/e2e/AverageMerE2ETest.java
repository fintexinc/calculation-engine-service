package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.result.fee.AverageMerResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.error.ErrorResponse;
import com.fintex.wm.commons.error.Notification;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.math.BigDecimal;
import java.util.List;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.etf;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.etfCa;
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
        etfCa("XBAL", 50_000),
        etfCa("VCNS", 75_000)));
    command.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return toJson(command);
  }

  @Override
  protected String requestBodyForPositiveMicScenario() {
    var command = new AverageMerCommand();
    command.setMetric(CalculationMetric.MER);
    command.setParameterTypes(List.of(FeeAggregationMode.FUNDS_ONLY, FeeAggregationMode.WHOLE_PORTFOLIO));
    command.setHoldings(List.of(
        etfCa("ETF-A", 300_000),
        etfCa("ETF-B", 100_000)));
    command.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return toJson(command);
  }

  @Override
  protected String micPositiveResponseBody() {
    // MIC returns fee fields in percentage form (e.g. 2.25 meaning 2.25%). FeesMapper converts to ratio form
    // (0.0225) for the rest of the engine — the expected output below is in ratio form. The currency datapoint is
    // required for MER-bearing holdings — without it, the FX-conversion step hard-fails with CUR-003.
    List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);
    var responseBody = List.of(
        new MicSecurityDataResponse(
            new SecurityIdentifier("ETF-A", FiIdentifierType.TICKER),
            new MicFeeData(
                null,
                new MicDatapoint(new BigDecimal("1.00"), providers),
                null,
                null,
                new MicCurrencyDatapoint(Currency.CAD, providers))),
        new MicSecurityDataResponse(
            new SecurityIdentifier("ETF-B", FiIdentifierType.TICKER),
            new MicFeeData(
                null,
                new MicDatapoint(new BigDecimal("2.00"), providers),
                null,
                null,
                new MicCurrencyDatapoint(Currency.CAD, providers))));
    return toJson(responseBody);
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    var command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setHoldings(List.of(etfCa("XBAL", 50_000)));
    command.setCurrency(Currency.CAD);
    return toJson(command);
  }

  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    AverageMerResult result = fromJson(responseBody, AverageMerResult.class);
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
    var micResponse = List.of(
        new MicSecurityDataResponse(
            new SecurityIdentifier(holdingId, FiIdentifierType.TICKER),
            new MicFeeData(
                null,
                null,
                null,
                null,
                new MicCurrencyDatapoint(Currency.USD, providers))));
    enqueueMicMockResponse(toJson(micResponse));

    var command = new AverageMerCommand();
    command.setMetric(CalculationMetric.MER);
    command.setParameterTypes(
        List.of(
            FeeAggregationMode.FUNDS_ONLY,
            FeeAggregationMode.WHOLE_PORTFOLIO));
    command.setHoldings(
        List.of(etf(
            holdingId,
            Country.USA,
            100_000)));
    command.setDataProviders(providers);

    var response = postCalculation(toJson(command));
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
    var micResponse = List.of(
        new MicSecurityDataResponse(
            new SecurityIdentifier(
                "ETF-MISSING-MER",
                FiIdentifierType.TICKER),
            new MicFeeData(
                new MicDatapoint(managementFeePercentage, providers),
                null,
                null,
                null,
                new MicCurrencyDatapoint(Currency.CAD, providers))));

    enqueueMicMockResponse(toJson(micResponse));
    var command = new AverageMerCommand();
    command.setMetric(CalculationMetric.MER);
    command.setParameterTypes(
        List.of(
            FeeAggregationMode.FUNDS_ONLY,
            FeeAggregationMode.WHOLE_PORTFOLIO));
    command.setHoldings(
        List.of(etfCa("ETF-MISSING-MER", 100_000)));
    command.setDataProviders(providers);

    var response = postCalculation(toJson(command));
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

  private static String toJson(Object value) {
    try {
      return OBJECT_MAPPER.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private static <T> T fromJson(String json, Class<T> type) {
    try {
      return OBJECT_MAPPER.readValue(json, type);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private record MicSecurityDataResponse(SecurityIdentifier identifier, MicFeeData data) {
  }

  private record MicFeeData(
      MicDatapoint managementFee,
      MicDatapoint managementExpenseRatio,
      MicDatapoint netExpenseRatio,
      MicDatapoint grossExpenseRatio,
      MicCurrencyDatapoint currency) {
  }

  private record MicDatapoint(BigDecimal value, List<DataProvider> dataProviders) {
  }

  // wm-commons CurrencyDatapoint stores the value in a field literally named "type" (not "value" as for
  // FloatDatapoint).
  // Jackson deserialization is property-name driven, so the JSON must say {"type": "CAD"} for the engine to receive it.
  private record MicCurrencyDatapoint(Currency type, List<DataProvider> dataProviders) {
  }
}

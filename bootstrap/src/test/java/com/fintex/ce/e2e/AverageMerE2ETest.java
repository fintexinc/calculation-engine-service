package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.AverageMerResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Tag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
class AverageMerE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  protected String metricPath() {
    return "mer";
  }

  @Override
  protected String requestBodyForSmsUnavailableScenario() {
    var command = new AverageMerCommand();
    command.setMetric(CalculationMetric.MER);
    command.setParameterTypes(List.of(FeeAggregationMode.FUNDS_ONLY, FeeAggregationMode.WHOLE_PORTFOLIO));
    command.setHoldings(List.of(
        holding("XBAL"),
        holding("VCNS")));
    command.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return toJson(command);
  }

  @Override
  protected String requestBodyForPositiveSmsScenario() {
    var command = new AverageMerCommand();
    command.setMetric(CalculationMetric.MER);
    command.setParameterTypes(List.of(FeeAggregationMode.FUNDS_ONLY, FeeAggregationMode.WHOLE_PORTFOLIO));
    command.setHoldings(List.of(holding("XBAL")));
    command.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return toJson(command);
  }

  @Override
  protected String smsPositiveResponseBody() {
    // SMS returns fee fields in percentage form (e.g. 2.25 meaning 2.25%). FeesMapper converts to ratio form
    // (0.0225) for the rest of the engine — the expected output below is in ratio form. The currency datapoint is
    // required for MER-bearing holdings — without it, the FX-conversion step hard-fails with CUR-003.
    List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);
    var responseBody = List.of(
        new SmsSecurityDataResponse(
            new SecurityIdentifier("XBAL", FiIdentifierType.TICKER),
            new SmsFeeData(
                new SmsDatapoint(new BigDecimal("1.25"), providers),
                new SmsDatapoint(new BigDecimal("2.25"), providers),
                new SmsDatapoint(new BigDecimal("2.10"), providers),
                new SmsDatapoint(new BigDecimal("2.50"), providers),
                new SmsCurrencyDatapoint(Currency.CAD, providers))));
    return toJson(responseBody);
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    var command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setHoldings(List.of(holding("XBAL")));
    command.setCurrency(Currency.CAD);
    return toJson(command);
  }

  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    AverageMerResult result = fromJson(responseBody, AverageMerResult.class);
    assertThat(result.getManagementExpenseRatio())
        .containsEntry(FeeAggregationMode.WHOLE_PORTFOLIO, new BigDecimal("0.0225000000"))
        .containsEntry(FeeAggregationMode.FUNDS_ONLY, new BigDecimal("0.0225000000"));
    assertThat(result.getWarnings()).isEmpty();
  }

  private static PortfolioHolding holding(String id) {
    return new PortfolioHolding(
        BigDecimal.valueOf(50_000),
        FinancialInstrumentType.ETF,
        Country.CANADA,
        new SecurityIdentifier(id, FiIdentifierType.TICKER));
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

  private record SmsSecurityDataResponse(SecurityIdentifier identifier, SmsFeeData data) {
  }

  private record SmsFeeData(
      SmsDatapoint managementFee,
      SmsDatapoint managementExpenseRatio,
      SmsDatapoint netExpenseRatio,
      SmsDatapoint grossExpenseRatio,
      SmsCurrencyDatapoint currency) {
  }

  private record SmsDatapoint(BigDecimal value, List<DataProvider> dataProviders) {
  }

  // wm-commons CurrencyDatapoint stores the value in a field literally named "type" (not "value" as for
  // FloatDatapoint).
  // Jackson deserialization is property-name driven, so the JSON must say {"type": "CAD"} for the engine to receive it.
  private record SmsCurrencyDatapoint(Currency type, List<DataProvider> dataProviders) {
  }
}

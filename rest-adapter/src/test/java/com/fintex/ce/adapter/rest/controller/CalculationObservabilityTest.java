package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculationObservabilityTest {

  @Test
  void shouldReturnCalculationResult_whenCalculationCompletes() {
    CalculationObservability observability = new CalculationObservability(ObservationRegistry.create());
    PeriodCommand command = periodCommand();
    BaseCalculationResult expected = new BaseCalculationResult() {};

    BaseCalculationResult actual = observability.observe(
        CalculationMetric.TRAILING_TOTAL_RETURNS.getValue(),
        command,
        observation -> expected);

    assertThat(actual).isSameAs(expected);
  }

  @Test
  void shouldPublishTraceContext_whenCalculationCompletes() {
    CapturingObservationHandler observationHandler = new CapturingObservationHandler();
    CalculationObservability traceObservability = new CalculationObservability(
        observationRegistry(observationHandler));
    PeriodCommand command = periodCommand();

    traceObservability.observe(
        CalculationMetric.TRAILING_TOTAL_RETURNS.getValue(),
        command,
        observation -> {
          observation.event(Observation.Event.of(CalculationObservability.VALIDATION_STARTED_EVENT));
          observation.event(Observation.Event.of(CalculationObservability.VALIDATION_COMPLETED_EVENT));
          return new BaseCalculationResult() {};
        });

    assertThat(observationHandler.eventNames)
        .contains(
            CalculationObservability.VALIDATION_STARTED_EVENT,
            CalculationObservability.VALIDATION_COMPLETED_EVENT,
            CalculationObservability.COMPLETED_EVENT);
    assertThat(observationHandler.stoppedContexts)
        .singleElement()
        .satisfies(context -> {
          assertThat(context.getName()).isEqualTo(CalculationObservability.CALCULATION_OBSERVATION_NAME);
          assertThat(lowCardinalityValue(context, CalculationObservability.METRIC_TAG))
              .isEqualTo(CalculationMetric.TRAILING_TOTAL_RETURNS.getValue());
          assertThat(lowCardinalityValue(context, CalculationObservability.OUTCOME_TAG))
              .isEqualTo(CalculationObservability.SUCCESS);
          assertThat(highCardinalityValue(context, CalculationObservability.REQUESTED_METRIC_KEY))
              .isEqualTo(CalculationMetric.TRAILING_TOTAL_RETURNS.getValue());
          assertThat(highCardinalityValue(context, CalculationObservability.PORTFOLIO_HOLDINGS_COUNT_KEY))
              .isEqualTo("2");
          assertThat(highCardinalityValue(context, CalculationObservability.BENCHMARK_HOLDINGS_COUNT_KEY))
              .isEqualTo("1");
          assertThat(highCardinalityValue(context, CalculationObservability.WARNINGS_COUNT_KEY)).isEqualTo("0");
        });
  }

  @Test
  void shouldPublishTraceContext_whenCalculationFails() {
    CapturingObservationHandler observationHandler = new CapturingObservationHandler();
    CalculationObservability traceObservability = new CalculationObservability(
        observationRegistry(observationHandler));
    PeriodCommand command = periodCommand();

    assertThatThrownBy(() -> traceObservability.observe(
        "not-supported",
        command,
        observation -> {
          throw new IllegalStateException("boom");
        }))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("boom");

    assertThat(observationHandler.eventNames).contains(CalculationObservability.FAILED_EVENT);
    assertThat(observationHandler.stoppedContexts)
        .singleElement()
        .satisfies(context -> {
          assertThat(lowCardinalityValue(context, CalculationObservability.METRIC_TAG))
              .isEqualTo(CalculationObservability.UNSUPPORTED);
          assertThat(lowCardinalityValue(context, CalculationObservability.OUTCOME_TAG))
              .isEqualTo(CalculationObservability.ERROR);
          assertThat(lowCardinalityValue(context, CalculationObservability.EXCEPTION_TAG))
              .isEqualTo(IllegalStateException.class.getSimpleName());
          assertThat(highCardinalityValue(context, CalculationObservability.REQUESTED_METRIC_KEY))
              .isEqualTo("not-supported");
        });
  }

  private static ObservationRegistry observationRegistry(CapturingObservationHandler observationHandler) {
    ObservationRegistry observationRegistry = ObservationRegistry.create();
    observationRegistry.observationConfig().observationHandler(observationHandler);
    return observationRegistry;
  }

  private static String lowCardinalityValue(Observation.Context context, String key) {
    return context.getLowCardinalityKeyValue(key).getValue();
  }

  private static String highCardinalityValue(Observation.Context context, String key) {
    return context.getHighCardinalityKeyValue(key).getValue();
  }

  private static PeriodCommand periodCommand() {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(CalculationMetric.TRAILING_TOTAL_RETURNS);
    command.setCurrency(Currency.CAD);
    command.setPeriods(Set.of("12"));
    command.setHoldings(List.of(holding("XIU.TO"), holding("VFV.TO")));
    command.setBenchmarkHoldings(List.of(holding("SPY")));
    return command;
  }

  private static PortfolioHolding holding(String id) {
    return new PortfolioHolding(
        BigDecimal.ONE,
        FinancialInstrumentType.ETF_CANADA,
        new SecurityIdentifier(id, FiIdentifierType.TICKER));
  }

  private static class CapturingObservationHandler implements ObservationHandler<Observation.Context> {

    private final List<Observation.Context> stoppedContexts = new ArrayList<>();
    private final List<String> eventNames = new ArrayList<>();

    @Override
    public void onStop(Observation.Context context) {
      stoppedContexts.add(context);
    }

    @Override
    public void onEvent(Observation.Event event, Observation.Context context) {
      eventNames.add(event.getName());
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
      return true;
    }
  }
}

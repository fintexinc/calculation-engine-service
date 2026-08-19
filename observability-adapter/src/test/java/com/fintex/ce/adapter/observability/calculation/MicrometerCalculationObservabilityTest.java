package com.fintex.ce.adapter.observability.calculation;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.result.composite.CompositeCalculationResult;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.etf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MicrometerCalculationObservabilityTest {

  @Test
  void shouldReturnCalculationResult_whenCalculationCompletes() {
    MicrometerCalculationObservability observability = new MicrometerCalculationObservability(
        ObservationRegistry.create(), statistics());
    BaseCalculationResult expected = new BaseCalculationResult() {};

    BaseCalculationResult actual = observability.observe(
        CalculationMetric.TRAILING_TOTAL_RETURNS.getValue(),
        periodCommand(CalculationMetric.TRAILING_TOTAL_RETURNS),
        () -> expected);

    assertThat(actual).isSameAs(expected);
  }

  @Test
  void shouldPublishTraceContext_whenCalculationCompletes() {
    CapturingObservationHandler observationHandler = new CapturingObservationHandler();
    MicrometerCalculationObservability observability = new MicrometerCalculationObservability(
        observationRegistry(observationHandler), statistics());

    observability.observe(
        CalculationMetric.TRAILING_TOTAL_RETURNS.getValue(),
        periodCommand(CalculationMetric.TRAILING_TOTAL_RETURNS),
        () -> new BaseCalculationResult() {});

    assertThat(observationHandler.eventNames)
        .as("phase events would each become a meaningless counter named after the observation")
        .isEmpty();
    assertThat(observationHandler.stoppedContexts)
        .singleElement()
        .satisfies(context -> {
          assertThat(context.getName()).isEqualTo(MicrometerCalculationObservability.REQUEST_OBSERVATION_NAME);
          assertThat(context.getContextualName()).isEqualTo("portfolio trailing-total-returns calculation");
          assertThat(lowCardinalityValue(context, MicrometerCalculationObservability.COMMAND_TAG))
              .isEqualTo(PeriodCommand.class.getSimpleName());
          assertThat(lowCardinalityValue(context, MicrometerCalculationObservability.OUTCOME_TAG))
              .isEqualTo(MicrometerCalculationObservability.SUCCESS);
          assertThat(lowCardinalityValue(context, MicrometerCalculationObservability.ERROR_TYPE_TAG))
              .isEqualTo(MicrometerCalculationObservability.NONE);
          assertThat(highCardinalityValue(context, MicrometerCalculationObservability.REQUESTED_METRIC_KEY))
              .isEqualTo(CalculationMetric.TRAILING_TOTAL_RETURNS.getValue());
          assertThat(highCardinalityValue(context, MicrometerCalculationObservability.PORTFOLIO_HOLDINGS_COUNT_KEY))
              .isEqualTo("2");
          assertThat(highCardinalityValue(context, MicrometerCalculationObservability.BENCHMARK_HOLDINGS_COUNT_KEY))
              .isEqualTo("1");
          assertThat(highCardinalityValue(context, MicrometerCalculationObservability.WARNINGS_COUNT_KEY)).isEqualTo(
              "0");
        });
  }

  @Test
  void shouldPublishTraceContext_whenCalculationFails() {
    CapturingObservationHandler observationHandler = new CapturingObservationHandler();
    MicrometerCalculationObservability observability = new MicrometerCalculationObservability(
        observationRegistry(observationHandler), statistics());

    assertThatThrownBy(() -> observability.observe(
        "not-supported",
        periodCommand(CalculationMetric.TRAILING_TOTAL_RETURNS),
        () -> {
          throw new IllegalStateException("boom");
        }))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("boom");

    assertThat(observationHandler.stoppedContexts)
        .singleElement()
        .satisfies(context -> {
          assertThat(context.getContextualName())
              .isEqualTo("portfolio " + MicrometerCalculationObservability.UNSUPPORTED + " calculation");
          assertThat(lowCardinalityValue(context, MicrometerCalculationObservability.OUTCOME_TAG))
              .isEqualTo(MicrometerCalculationObservability.ERROR);
          assertThat(lowCardinalityValue(context, MicrometerCalculationObservability.ERROR_TYPE_TAG))
              .isEqualTo(IllegalStateException.class.getSimpleName());
          assertThat(highCardinalityValue(context, MicrometerCalculationObservability.REQUESTED_METRIC_KEY))
              .isEqualTo("not-supported");
        });
  }

  @Test
  void shouldUseTheSameObservationWithoutMetricTag_whenCompositeRequestIsObserved() {
    CapturingObservationHandler observationHandler = new CapturingObservationHandler();
    MicrometerCalculationObservability observability = new MicrometerCalculationObservability(
        observationRegistry(observationHandler), statistics());
    List<CalculationCommand> commands = List.of(
        periodCommand(CalculationMetric.TRAILING_TOTAL_RETURNS),
        periodCommand(CalculationMetric.ALPHA));

    observability.observeComposite(commands, () -> CompositeCalculationResult.builder()
        .results(Map.of())
        .failures(Map.of())
        .build());

    assertThat(observationHandler.stoppedContexts)
        .singleElement()
        .satisfies(context -> {
          assertThat(context.getName())
              .as("both endpoints must share one request timer name")
              .isEqualTo(MicrometerCalculationObservability.REQUEST_OBSERVATION_NAME);
          assertThat(context.getLowCardinalityKeyValues())
              .as("no tag may carry a metric name, least of all the literal 'composite'")
              .noneMatch(keyValue -> keyValue.getKey().equals("calculation.metric")
                  || keyValue.getValue().equals(MicrometerCalculationObservability.COMPOSITE));
          assertThat(highCardinalityValue(context, MicrometerCalculationObservability.REQUESTED_METRIC_KEY))
              .isEqualTo("trailing-total-returns,alpha");
          assertThat(highCardinalityValue(context, MicrometerCalculationObservability.REQUESTED_METRICS_COUNT_KEY))
              .isEqualTo("2");
          assertThat(highCardinalityValue(context, MicrometerCalculationObservability.PORTFOLIO_HOLDINGS_COUNT_KEY))
              .isEqualTo("4");
        });
  }

  private static CalculationMetricStatistics statistics() {
    return new CalculationMetricStatistics(new SimpleMeterRegistry());
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

  private static PeriodCommand periodCommand(CalculationMetric metric) {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(metric);
    command.setCurrency(Currency.CAD);
    command.setPeriods(Set.of(TimePeriod.ONE_YR));
    command.setHoldings(List.of(etf("XIU.TO", Country.CANADA, 1), etf("VFV.TO", Country.CANADA, 1)));
    command.setBenchmarkHoldings(List.of(etf("SPY", Country.CANADA, 1)));
    return command;
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

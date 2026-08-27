package ca.tangerine.pce.port.observability;

import java.time.Duration;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;

/**
 * Reports how long one metric took to calculate. Implemented by the observability adapter and called by the
 * orchestrator around the single point where a calculation service actually runs, which is the only place that can time
 * a member of a composite request separately from its siblings.
 */
public interface CalculationDurationRecorder {

  CalculationDurationRecorder NO_OP = new CalculationDurationRecorder() {

    @Override
    public void recordSuccess(CalculationMetric metric, Duration duration) {
    }

    @Override
    public void recordFailure(CalculationMetric metric, Duration duration) {
    }
  };

  void recordSuccess(CalculationMetric metric, Duration duration);

  void recordFailure(CalculationMetric metric, Duration duration);
}

package com.fintex.ce.port.observability;

import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.result.composite.CompositeCalculationResult;
import com.fintex.ce.model.dto.command.CalculationCommand;

import java.util.List;
import java.util.function.Supplier;

/**
 * Observes the dispatch of a calculation: the caller hands over the work, and whatever telemetry the deployment
 * publishes — traces, per-metric counters, latencies, error and warning codes — is derived from running it. The result
 * and any exception pass straight through, so wrapping a call cannot change its outcome.
 *
 * <p>
 * What is handed here must already have passed metric resolution and request validation, and the supplied action must
 * be the dispatch alone. A request rejected at the boundary never reached a calculator, and counting it as a failed
 * execution would let a client sending malformed requests raise the failure rate of a metric that is working perfectly.
 *
 * <p>
 * A composite request is one observation with many member metrics, not many requests: implementations report the
 * per-metric numbers against the metric that actually ran, so the endpoint a client happened to call leaves no trace in
 * the per-metric view.
 */
public interface CalculationObservability {

  BaseCalculationResult observe(
      String metricName,
      CalculationCommand command,
      Supplier<BaseCalculationResult> action);

  CompositeCalculationResult observeComposite(
      List<CalculationCommand> commands,
      Supplier<CompositeCalculationResult> action);
}

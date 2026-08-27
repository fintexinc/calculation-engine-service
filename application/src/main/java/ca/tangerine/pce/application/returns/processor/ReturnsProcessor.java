package ca.tangerine.pce.application.returns.processor;

import ca.tangerine.pce.application.returns.ProcessingCase;
import ca.tangerine.pce.application.returns.ProcessingContext;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.model.domain.calculation.returns.ReturnsData;

/**
 * Single stage in a returns-processing pipeline. Each implementation transforms an immutable {@link ReturnsSnapshot}
 * into a new snapshot — validations layer additional errors, cuts narrow the date window, FX conversion replaces the
 * per-holding series with currency-converted ones.
 *
 * <p>
 * The orchestrator injects {@code List<ReturnsProcessor>} (Spring orders the list by
 * {@link org.springframework.core.annotation.Order}), filters once at startup by {@link #isApplicable(ProcessingCase)}
 * into a pipeline per {@link ProcessingCase}, then for every request applies the resulting pipeline as a fold over the
 * snapshot.
 * </p>
 */
public interface ReturnsProcessor {

  <T extends ReturnsData> ReturnsSnapshot<T> process(ReturnsSnapshot<T> snapshot, ProcessingContext context);

  boolean isApplicable(ProcessingCase processingCase);
}

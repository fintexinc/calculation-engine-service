package ca.tangerine.pce.application.validation;

import java.util.List;

import ca.tangerine.pce.application.returns.ProcessingContext;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.application.returns.processor.ReturnsProcessor;
import ca.tangerine.pce.model.domain.calculation.returns.ReturnsData;
import ca.tangerine.pce.model.error.exceptions.BasePceException;

/**
 * Common base for {@link ReturnsProcessor} implementations whose only effect on a snapshot is to layer additional
 * errors onto it.
 *
 * <p>
 * Subclasses implement {@link #collectErrors(ReturnsSnapshot, ProcessingContext)} to inspect the snapshot and the
 * request context and return any errors they detect. The base class threads those errors back into a new snapshot via
 * {@link ReturnsSnapshot#withAddedErrors(List)} so the rest of the pipeline observes them but is not rewritten.
 * Validations never throw — fatal-vs-warning classification is the {@code ReturnsErrorPolicy}'s job, applied once at
 * the end of the pipeline.
 * </p>
 */
public abstract class AbstractReturnsValidation implements ReturnsProcessor {

  @Override
  public final <T extends ReturnsData> ReturnsSnapshot<T> process(ReturnsSnapshot<T> snapshot,
      ProcessingContext context) {
    List<BasePceException> additionalErrors = collectErrors(snapshot, context);
    return snapshot.withAddedErrors(additionalErrors);
  }

  protected abstract <T extends ReturnsData> List<BasePceException> collectErrors(ReturnsSnapshot<T> snapshot,
      ProcessingContext context);
}

package com.fintex.ce.application.validation;

import com.fintex.ce.application.returns.ProcessingContext;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.processor.ReturnsProcessor;
import com.fintex.ce.model.domain.calculation.returns.ReturnsData;
import com.fintex.ce.model.error.exceptions.BasePceException;

import java.util.List;

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
